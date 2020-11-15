package io.github.jtagscherer.falloutplugin.features.exposure;

@FunctionalInterface
interface TargetLocationFunction<DistancedLocation, World, Boolean> {
    Boolean apply(DistancedLocation location, World world);
}
