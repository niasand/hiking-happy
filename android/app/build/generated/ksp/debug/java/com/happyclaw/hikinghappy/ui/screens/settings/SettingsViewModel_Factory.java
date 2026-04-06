package com.happyclaw.hikinghappy.ui.screens.settings;

import android.content.Context;
import com.happyclaw.hikinghappy.domain.UserPreferencesRepository;
import com.happyclaw.hikinghappy.service.SyncService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  private final Provider<SyncService> syncServiceProvider;

  private final Provider<Context> contextProvider;

  public SettingsViewModel_Factory(
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<SyncService> syncServiceProvider, Provider<Context> contextProvider) {
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
    this.syncServiceProvider = syncServiceProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(preferencesRepositoryProvider.get(), syncServiceProvider.get(), contextProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<SyncService> syncServiceProvider, Provider<Context> contextProvider) {
    return new SettingsViewModel_Factory(preferencesRepositoryProvider, syncServiceProvider, contextProvider);
  }

  public static SettingsViewModel newInstance(UserPreferencesRepository preferencesRepository,
      SyncService syncService, Context context) {
    return new SettingsViewModel(preferencesRepository, syncService, context);
  }
}
