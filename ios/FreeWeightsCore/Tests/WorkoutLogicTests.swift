import XCTest
@testable import FreeWeightsCore

final class WorkoutLogicTests: XCTestCase {
    func testPlateCalculatorIncludesBar() {
        let load = calculatePlates(targetWeight: 225, barWeight: 45, availablePlates: [45, 25, 10, 5, 2.5])
        XCTAssertEqual(load.platesPerSide, [45, 45])
        XCTAssertEqual(load.loadedWeight, 225, accuracy: 0.001)
        XCTAssertTrue(load.isExact)
    }

    func testWarmupsReachEightyPercentWithoutExceedingIt() {
        let exercise = ExercisePlan(
            name: "Squat",
            workingWeight: 225,
            warmupSets: [
                WarmupSetPlan(reps: 10, weightPercent: 0),
                WarmupSetPlan(reps: 5, weightPercent: 60),
                WarmupSetPlan(reps: 3, weightPercent: 80),
            ]
        )
        let sets = prescribedLiftSets(exercise: exercise, workingWeight: 225, barWeight: 45, availablePlates: [45, 25, 10, 5, 2.5])
        XCTAssertEqual(sets.prefix(3).map(\.weight), [45, 135, 180])
        XCTAssertTrue(sets.prefix(3).allSatisfy(\.isWarmup))
    }

    func testPyramidSupportsPerSetRepsAndOffsets() {
        let exercise = ExercisePlan(
            name: "Bench",
            workingWeight: 135,
            workSets: [
                WorkSetPlan(reps: 10, weightOffset: -10),
                WorkSetPlan(reps: 8, weightOffset: 0),
                WorkSetPlan(reps: 6, weightOffset: 10),
                WorkSetPlan(reps: 8, weightOffset: 0),
            ]
        )
        let sets = prescribedLiftSets(exercise: exercise, workingWeight: 135, barWeight: 45, availablePlates: [45, 25, 10, 5, 2.5])
        XCTAssertEqual(sets.map(\.reps), [10, 8, 6, 8])
        XCTAssertEqual(sets.map(\.weight), [125, 135, 145, 135])
    }

    func testWarmupsAreExcludedFromVolume() {
        let log = WorkoutLog(
            exerciseId: "squat",
            exerciseName: "Squat",
            sets: 2,
            reps: 5,
            weight: 145,
            setResults: [
                WorkoutSetResult(exerciseTrackingId: "squat", setNumber: 1, reps: 10, weight: 45, succeeded: true, isWarmup: true),
                WorkoutSetResult(exerciseTrackingId: "squat", setNumber: 2, reps: 5, weight: 135, succeeded: true),
                WorkoutSetResult(exerciseTrackingId: "squat", setNumber: 3, reps: 3, weight: 145, succeeded: true),
            ]
        )
        XCTAssertEqual(log.volume, 1110, accuracy: 0.001)
    }
}
