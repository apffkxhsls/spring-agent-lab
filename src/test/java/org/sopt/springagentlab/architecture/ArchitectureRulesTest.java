package org.sopt.springagentlab.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureRulesTest {

    private static final String BASE = "org.sopt.springagentlab.";
    private static final String DOMAIN = BASE + "domain..";
    private static final String APPLICATION = BASE + "application..";
    private static final String PRESENTATION = BASE + "presentation..";
    private static final String INFRASTRUCTURE = BASE + "infrastructure..";

    // Only production classes are subjects of the rules. Configuration is imported
    // but has no outgoing-dependency restriction: it wires the application together.
    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("org.sopt.springagentlab");

    @Test
    void domainMustNotDependOnOtherLayersOrSpringOrJpa() {
        noClasses().that().resideInAPackage(DOMAIN)
                .should().dependOnClassesThat(
                        resideInAPackage("org.sopt.springagentlab..").and(not(resideInAPackage(DOMAIN)))
                                .or(resideInAnyPackage("org.springframework..", "jakarta.persistence..",
                                        "javax.persistence..", "org.hibernate..")))
                .because("domain must protect business rules independently of other layers and frameworks")
                .check(CLASSES);
    }

    @Test
    void applicationMustNotDependOnOuterLayersOrHttpOrJpa() {
        noClasses().that().resideInAPackage(APPLICATION)
                .should().dependOnClassesThat().resideInAnyPackage(
                        PRESENTATION, INFRASTRUCTURE,
                        "org.springframework.http..", "org.springframework.web..",
                        "jakarta.servlet..", "javax.servlet..", "java.net.http..",
                        "jakarta.persistence..", "javax.persistence..",
                        "org.springframework.data.jpa..", "org.hibernate..")
                .because("application contracts and services must not use HTTP or persistence implementation types")
                .check(CLASSES);
    }

    @Test
    void presentationMustNotDependOnInfrastructure() {
        noClasses().that().resideInAPackage(PRESENTATION)
                .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE)
                .because("presentation must reach persistence through input ports")
                .check(CLASSES);
    }

    @Test
    void infrastructureMustNotDependOnPresentation() {
        noClasses().that().resideInAPackage(INFRASTRUCTURE)
                .should().dependOnClassesThat().resideInAPackage(PRESENTATION)
                .because("persistence must not know HTTP controllers or DTOs")
                .check(CLASSES);
    }

    @Test
    void presentationMustUseOnlyApplicationInputPorts() {
        noClasses().that().resideInAPackage(PRESENTATION)
                .should().dependOnClassesThat(resideInAPackage(APPLICATION)
                        .and(not(resideInAPackage(BASE + "application.post.port.in.."))))
                .because("presentation must use input ports instead of output ports or service implementations")
                .check(CLASSES);
    }
}
