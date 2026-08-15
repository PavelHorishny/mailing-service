package com.company.mailing_service.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.company.mailing_service", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {
    @ArchTest
    static final ArchRule domainShouldNotDependOnInfrastructure = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule serviceShouldNotDependOnInfrastructure = noClasses()
            .that().resideInAPackage("com.company.mailing_service.service..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
}
