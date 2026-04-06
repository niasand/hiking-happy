package com.happyclaw.hikinghappy.di;

import com.happyclaw.hikinghappy.data.local.HikingDatabase;
import com.happyclaw.hikinghappy.data.local.dao.ActivityRecordDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DatabaseModule_ProvideActivityRecordDaoFactory implements Factory<ActivityRecordDao> {
  private final Provider<HikingDatabase> databaseProvider;

  public DatabaseModule_ProvideActivityRecordDaoFactory(Provider<HikingDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ActivityRecordDao get() {
    return provideActivityRecordDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideActivityRecordDaoFactory create(
      Provider<HikingDatabase> databaseProvider) {
    return new DatabaseModule_ProvideActivityRecordDaoFactory(databaseProvider);
  }

  public static ActivityRecordDao provideActivityRecordDao(HikingDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideActivityRecordDao(database));
  }
}
