// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "FreeWeightsCore",
    platforms: [.iOS(.v17)],
    products: [.library(name: "FreeWeightsCore", targets: ["FreeWeightsCore"])],
    targets: [
        .target(name: "FreeWeightsCore", path: "FreeWeightsCore/Sources"),
        .testTarget(name: "FreeWeightsCoreTests", dependencies: ["FreeWeightsCore"], path: "FreeWeightsCore/Tests"),
    ]
)
