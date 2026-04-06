package com.happyclaw.hikinghappy.data.repository;

import com.happyclaw.hikinghappy.data.local.dao.ActivityRecordDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ActivityRepositoryImpl_Factory implements Factory<ActivityRepositoryImpl> {
  private final Provider<ActivityRecordDao> daoProvider;

  public ActivityRepositoryImpl_Factory(Provider<ActivityRecordDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public ActivityRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static ActivityRepositoryImpl_Factory create(Provider<ActivityRecordDao> daoProvider) {
    return new ActivityRepositoryImpl_Factory(daoProvider);
  }

  public static ActivityRepositoryImpl newInstance(ActivityRecordDao dao) {
    return new ActivityRepositoryImpl(dao);
  }
}
