$ErrorActionPreference = 'Stop'
$utf8 = New-Object System.Text.UTF8Encoding($false)
[Console]::OutputEncoding = $utf8
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$logDirectory = Join-Path $projectRoot 'build/codex-hooks'
$runId = [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssfffZ') + '-' + [Guid]::NewGuid().ToString('N')
$logPath = Join-Path $logDirectory ($runId + '.gradle.log')
$resultPath = Join-Path $logDirectory ($runId + '.result.json')
$event = $null
$gradleExitCode = $null
$status = 'verification_incomplete'
$process = $null

try {
    [IO.Directory]::CreateDirectory($logDirectory) | Out-Null
    $event = [Console]::In.ReadToEnd() | ConvertFrom-Json
    if ($event.hook_event_name -ne 'Stop' -or $event.stop_hook_active -isnot [bool]) {
        throw 'Expected Stop input with a boolean stop_hook_active field.'
    }

    $gradlePath = Join-Path $projectRoot 'gradlew.bat'
    if (-not (Test-Path -LiteralPath $gradlePath -PathType Leaf)) {
        throw 'Project Gradle wrapper was not found.'
    }

    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $env:ComSpec
    # cmd redirects both streams to the log: Gradle never writes to hook stdout.
    $startInfo.Arguments = '/d /s /c ""{0}" test --tests org.sopt.springagentlab.architecture.ArchitectureRulesTest --console=plain --rerun-tasks > "{1}" 2>&1"' -f $gradlePath, $logPath
    $startInfo.WorkingDirectory = $projectRoot
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    $startedAt = [DateTime]::UtcNow
    $process.Start() | Out-Null
    if (-not $process.WaitForExit(240000)) {
        # Kill only the process tree created by this invocation, before hook timeout.
        & taskkill.exe /PID $process.Id /T /F *> $null
        throw 'Architecture check exceeded its 240-second execution limit.'
    }
    $gradleExitCode = $process.ExitCode

    if ($gradleExitCode -eq 0) {
        $status = 'passed'
        $response = @{}
    }
    else {
        # Gradle's console summary may omit the dependency itself. Include fresh
        # JUnit failure messages in the log and continuation, never stale results.
        $reportPath = Join-Path $projectRoot 'build/test-results/test/TEST-org.sopt.springagentlab.architecture.ArchitectureRulesTest.xml'
        if ((Test-Path -LiteralPath $reportPath) -and
                (Get-Item -LiteralPath $reportPath).LastWriteTimeUtc -ge $startedAt) {
            try {
                [xml]$report = [IO.File]::ReadAllText($reportPath)
                $failureDetails = (@($report.testsuite.testcase | Where-Object { $_.failure } |
                        ForEach-Object { $_.name + ': ' + $_.failure.message })) -join "`n"
                [IO.File]::AppendAllText($logPath, "`nJUnit architecture failure details:`n$failureDetails`n", $utf8)
            }
            catch {
                [IO.File]::AppendAllText($logPath, "`nCould not read failure details. Report: $reportPath`n", $utf8)
            }
        }
        $tail = (Get-Content -LiteralPath $logPath -Tail 35) -join "`n"
        if ($tail.Length -gt 7000) { $tail = $tail.Substring($tail.Length - 7000) }
        $summary = "Architecture verification failed (Gradle exit $gradleExitCode). Log: $logPath`n$tail"
        if ($event.stop_hook_active) {
            $status = 'verification_incomplete'
            $response = @{
                continue = $false
                stopReason = "VERIFICATION INCOMPLETE: retry failed. $summary"
                systemMessage = "VERIFICATION INCOMPLETE: automatic retry stopped. Do not report success. $summary"
            }
        }
        else {
            $status = 'retry_requested'
            $response = @{
                decision = 'block'
                reason = "$summary`nRead the log, fix the cause without weakening the rules, and recheck. If blocked, report verification incomplete. Only one automatic continuation is permitted."
            }
        }
    }
}
catch {
    # Script/setup errors must not masquerade as a passing architecture check.
    $details = $_.Exception.Message
    $response = @{
        continue = $false
        stopReason = "VERIFICATION INCOMPLETE: $details"
        systemMessage = "VERIFICATION INCOMPLETE: hook execution error. $details Log (if created): $logPath"
    }
}
finally {
    if ($null -ne $process) { $process.Dispose() }
}

try {
    $record = [ordered]@{
        timestamp_utc = [DateTime]::UtcNow.ToString('o')
        session_id = $event.session_id
        turn_id = $event.turn_id
        hook_event_name = $event.hook_event_name
        stop_hook_active = $event.stop_hook_active
        status = $status
        gradle_exit_code = $gradleExitCode
        test_filter = 'org.sopt.springagentlab.architecture.ArchitectureRulesTest'
        log_path = $logPath
        hook_response = $response
    }
    [IO.File]::WriteAllText($resultPath, ($record | ConvertTo-Json -Depth 6), $utf8)
}
catch {
    $response = @{
        continue = $false
        stopReason = 'VERIFICATION INCOMPLETE: unable to persist the hook result.'
        systemMessage = "VERIFICATION INCOMPLETE: result write failed: $($_.Exception.Message)"
    }
}

# Stop expects JSON, while exit 0 lets Codex interpret decision/continue fields.
[Console]::Out.WriteLine(($response | ConvertTo-Json -Depth 6 -Compress))
exit 0
