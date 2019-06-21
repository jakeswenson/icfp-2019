load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_jvm_binary", "kt_jvm_test")

kt_jvm_binary(
    name = "misfits",
    main_class = 'AppKt',
    srcs = glob(["src/main/kotlin/**/*.kt"]),
    deps = [
        # "//path/to/dependency",
    ],
)

kt_jvm_test(
    name = "misfits_test",
    srcs = glob(["src/test/kotlin/**/*.kt"]),
    deps = [
        ":misfits",
        "@jake//:org_junit_jupiter_junit_jupiter_api",
        # "//path/to/dependency",
    ],
)
