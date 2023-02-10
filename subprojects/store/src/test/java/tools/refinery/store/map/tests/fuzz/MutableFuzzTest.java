package tools.refinery.store.map.tests.fuzz;

import static org.junit.jupiter.api.Assertions.fail;
import static tools.refinery.store.map.tests.fuzz.utils.FuzzTestCollections.*;

import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import tools.refinery.store.map.ContinousHashProvider;
import tools.refinery.store.map.VersionedMapStore;
import tools.refinery.store.map.VersionedMapStoreImpl;
import tools.refinery.store.map.internal.VersionedMapImpl;
import tools.refinery.store.map.tests.fuzz.utils.FuzzTestUtils;
import tools.refinery.store.map.tests.utils.MapTestEnvironment;

class MutableFuzzTest {
	private void runFuzzTest(String scenario, int seed, int steps, int maxKey, int maxValue,
							 boolean nullDefault, boolean evilHash) {
		String[] values = MapTestEnvironment.prepareValues(maxValue, nullDefault);
		ContinousHashProvider<Integer> chp = MapTestEnvironment.prepareHashProvider(evilHash);

		VersionedMapStore<Integer, String> store = new VersionedMapStoreImpl<>(chp, values[0]);
		VersionedMapImpl<Integer, String> sut = (VersionedMapImpl<Integer, String>) store.createMap();
		MapTestEnvironment<Integer, String> e = new MapTestEnvironment<>(sut);

		Random r = new Random(seed);

		iterativeRandomPuts(scenario, steps, maxKey, values, e, r);
	}

	private void iterativeRandomPuts(String scenario, int steps, int maxKey, String[] values,
									 MapTestEnvironment<Integer, String> e, Random r) {
		for (int i = 0; i < steps; i++) {
			int index = i + 1;
			int nextKey = r.nextInt(maxKey);
			String nextValue = values[r.nextInt(values.length)];

			try {
				e.put(nextKey, nextValue);
				e.checkEquivalence(scenario + ":" + index);
			} catch (Exception exception) {
				exception.printStackTrace();
				fail(scenario + ":" + index + ": exception happened: " + exception);
			}
			MapTestEnvironment.printStatus(scenario, index, steps, null);
		}
	}

	@ParameterizedTest(name = "Mutable {index}/{0} Steps={1} Keys={2} Values={3} defaultNull={4} seed={5} " +
			"evil-hash={6}")
	@MethodSource
	@Timeout(value = 10)
	@Tag("fuzz")
	void parametrizedFuzz(int ignoredTests, int steps, int noKeys, int noValues, boolean defaultNull, int seed,
						  boolean evilHash) {
		runFuzzTest(
				"MutableS" + steps + "K" + noKeys + "V" + noValues + "s" + seed + "H" + (evilHash ? "Evil" : "Normal"),
				seed, steps, noKeys, noValues, defaultNull, evilHash);
	}

	static Stream<Arguments> parametrizedFuzz() {
		return FuzzTestUtils.permutationWithSize(stepCounts, keyCounts, valueCounts, nullDefaultOptions,
				randomSeedOptions, evilHashOptions);
	}

	@ParameterizedTest(name = "Mutable {index}/{0} Steps={1} Keys={2} Values={3} nullDefault={4} seed={5} " +
			"evil-hash={6}")
	@MethodSource
	@Tag("fuzz")
	@Tag("slow")
	void parametrizedSlowFuzz(int ignoredTests, int steps, int noKeys, int noValues, boolean nullDefault, int seed,
							  boolean evilHash) {
		runFuzzTest(
				"MutableS" + steps + "K" + noKeys + "V" + noValues + "s" + seed + "H" + (evilHash ? "Evil" : "Normal"),
				seed, steps, noKeys, noValues, nullDefault, evilHash);
	}

	static Stream<Arguments> parametrizedSlowFuzz() {
		return FuzzTestUtils.changeStepCount(parametrizedFuzz(), 1);
	}
}
