package GTResources;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Machine-local paths supplied by GT-Ecosystem.
 *
 * The scheduler historically embedded paths from one development machine.
 * Environment variables keep it usable both inside GT-Ecosystem and as a
 * standalone repository.
 */
public final class EcosystemPaths {

    private EcosystemPaths() {
    }

    private static String environmentOrDefault(String name, Path fallback) {
        String value = System.getenv(name);

        if (value != null && !value.isBlank()) {
            return Paths.get(value).toAbsolutePath().normalize().toString();
        }

        return fallback.toAbsolutePath().normalize().toString();
    }

    public static Path schedulerRoot() {
        String configured = System.getenv("GT_SCHEDULER_ROOT");

        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured).toAbsolutePath().normalize();
        }

        return Paths.get(System.getProperty("user.dir"))
                .toAbsolutePath()
                .normalize();
    }

    private static Path repositoriesRoot() {
        Path schedulerParent = schedulerRoot().getParent();

        if (schedulerParent == null) {
            return schedulerRoot();
        }

        return schedulerParent;
    }

    public static String vtuneCommand() {
        String configured = System.getenv("GT_VTUNE_COMMAND");
        return configured == null || configured.isBlank()
                ? "vtune"
                : configured;
    }

    public static String graalJava() {
        return environmentOrDefault(
                "GT_GRAAL_JAVA",
                repositoriesRoot()
                        .resolve("graal-instrumentation")
                        .resolve("vm/latest_graalvm_home/bin/java"));
    }

    public static String awfyJar() {
        return environmentOrDefault(
                "GT_AWFY_JAR",
                repositoriesRoot()
                        .resolve("are-we-fast-yet")
                        .resolve("benchmarks/Java/benchmarks.jar"));
    }

    public static String awfySourceDirectory() {
        return environmentOrDefault(
                "GT_AWFY_SOURCE_DIR",
                repositoriesRoot()
                        .resolve("are-we-fast-yet")
                        .resolve("benchmarks/Java/src"));
    }

    public static String renaissanceJar() {
        return environmentOrDefault(
                "GT_RENAISSANCE_JAR",
                repositoriesRoot()
                        .resolve("renaissance")
                        .resolve("renaissance-gpl-0.16.1.jar"));
    }

    public static String benchmarkMetadata() {
        return environmentOrDefault(
                "GT_AWFY_METADATA",
                schedulerRoot().resolve("GTResources/AWFY_Benchmarks.json"));
    }
}
