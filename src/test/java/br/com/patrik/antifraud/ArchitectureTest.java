package br.com.patrik.antifraud;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "br.com.patrik.antifraud")
public class ArchitectureTest {
    @ArchTest
    static final ArchRule domain_should_not_depend_on_other_layers =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..application..",
                            "..infrastructure..",
                            "..antifraud.gateway.."
                    );

    @ArchTest
    static final ArchRule services_should_be_in_service_package =
            classes()
                    .that().haveSimpleNameEndingWith("Service")
                    .should().resideInAPackage("..domain.service..");

    @ArchTest
    static final ArchRule infrastructure_should_depend_on_domain =
            classes()
                    .that().resideInAPackage("..infrastructure..")
                    .should().dependOnClassesThat().resideInAPackage("..domain..");
}