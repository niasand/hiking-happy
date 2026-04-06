package com.happyclaw.hikinghappy.service;

import android.content.Context;
import com.happyclaw.hikinghappy.data.repository.ActivityRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SyncService_Factory implements Factory<SyncService> {
  private final Provider<Context> contextProvider;

  private final Provider<ActivityRepository> activityRepositoryProvider;

  public SyncService_Factory(Provider<Context> contextProvider,
      Provider<ActivityRepository> activityRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.activityRepositoryProvider = activityRepositoryProvider;
  }

  @Override
  public SyncService get() {
    return newInstance(contextProvider.get(), activityRepositoryProvider.get());
  }

  public static SyncService_Factory create(Provider<Context> contextProvider,
      Provider<ActivityRepository> activityRepositoryProvider) {
    return new SyncService_Factory(contextProvider, activityRepositoryProvider);
  }

  public static SyncService newInstance(Context context, ActivityRepository activityRepository) {
    return new SyncService(context, activityRepository);
  }
}
