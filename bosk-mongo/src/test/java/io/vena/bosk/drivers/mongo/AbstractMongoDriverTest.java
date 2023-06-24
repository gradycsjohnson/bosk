package io.vena.bosk.drivers.mongo;

import com.mongodb.MongoClientSettings;
import io.vena.bosk.Bosk;
import io.vena.bosk.Catalog;
import io.vena.bosk.CatalogReference;
import io.vena.bosk.DriverFactory;
import io.vena.bosk.Entity;
import io.vena.bosk.Identifier;
import io.vena.bosk.Listing;
import io.vena.bosk.ListingEntry;
import io.vena.bosk.Reference;
import io.vena.bosk.SideTable;
import io.vena.bosk.annotations.ReferencePath;
import io.vena.bosk.drivers.mongo.MongoDriverSettings.MongoDriverSettingsBuilder;
import io.vena.bosk.drivers.state.TestEntity;
import io.vena.bosk.exceptions.InvalidTypeException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vena.bosk.drivers.mongo.SingleDocumentMongoDriver.COLLECTION_NAME;
import static java.util.concurrent.TimeUnit.SECONDS;

abstract class AbstractMongoDriverTest {
	protected static final Identifier entity123 = Identifier.from("123");
	protected static final Identifier entity124 = Identifier.from("124");
	protected static final Identifier rootID = Identifier.from("root");

	protected static MongoService mongoService;
	protected DriverFactory<TestEntity> driverFactory;
	protected Deque<Runnable> tearDownActions;
	protected final MongoDriverSettings driverSettings;

	public AbstractMongoDriverTest(MongoDriverSettingsBuilder driverSettings) {
		this.driverSettings = driverSettings.build();
	}


	@BeforeAll
	static void setupMongoConnection() {
		mongoService = new MongoService();
	}

	@BeforeEach
	void setupDriverFactory() {
		driverFactory = createDriverFactory();

		// Start with a clean slate
		mongoService.client()
			.getDatabase(driverSettings.database())
			.getCollection(COLLECTION_NAME)
			.drop();
	}

	@BeforeEach
	void clearTearDown(TestInfo testInfo) {
		logTest("/=== Start", testInfo);
		tearDownActions = new ArrayDeque<>();
//		tearDownActions.addLast(() ->  {
//			try {
//				LOGGER.debug("Sleeping after teardown");
//				Thread.sleep(10_000);
//			} catch (InterruptedException e) {
//				LOGGER.debug("Interrupted", e);
//				Thread.interrupted();
//			} finally {
//				LOGGER.debug("Done sleeping");
//			}
//		});
	}

	@AfterEach
	void runTearDown(TestInfo testInfo) {
		tearDownActions.forEach(Runnable::run);
		logTest("\\=== Done", testInfo);
	}

	private static void logTest(String verb, TestInfo testInfo) {
		String method =
			testInfo.getTestClass().map(Class::getSimpleName).orElse(null)
				+ "."
				+ testInfo.getTestMethod().map(Method::getName).orElse(null);
		LOGGER.info("{} {} {}", verb, method, testInfo.getDisplayName());
	}


	protected TestEntity initialRoot(Bosk<TestEntity> testEntityBosk) throws InvalidTypeException {
		Refs refs = testEntityBosk.buildReferences(Refs.class);
		return new TestEntity(rootID,
			rootID.toString(),
			Catalog.of(
				TestEntity.empty(entity123, refs.childCatalog(entity123)),
				TestEntity.empty(entity124, refs.childCatalog(entity124))
			),
			Listing.of(refs.catalog(), entity123),
			SideTable.empty(refs.catalog()),
			Optional.empty()
		);
	}

	protected <E extends Entity> DriverFactory<E> createDriverFactory() {
		return (bosk, downstream) -> {
			MongoDriver<E> driver = MongoDriver.<E>factory(
				MongoClientSettings.builder(mongoService.clientSettings())
					.applyToClusterSettings(builder -> {
						builder.serverSelectionTimeout(5, SECONDS);
					})
					.applyToSocketSettings(builder -> {
						// We're testing timeouts. Let's not wait too long.
						builder.readTimeout(5, SECONDS);
					})
					.build(),
				driverSettings,
				new BsonPlugin()
			).build(bosk, downstream);
			tearDownActions.addFirst(driver::close);
			return driver;
		};
	}

	public interface Refs {
		@ReferencePath("/catalog")
		CatalogReference<TestEntity> catalog();

		@ReferencePath("/listing/-entity-")
		Reference<ListingEntry> listingEntry(Identifier entity);

		@ReferencePath("/catalog/-child-/catalog")
		CatalogReference<TestEntity> childCatalog(Identifier child);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMongoDriverTest.class);
}