package com.happyclaw.hikinghappy.ui.screens.instruments;

import com.happyclaw.hikinghappy.domain.UserPreferencesRepository;
import com.happyclaw.hikinghappy.service.LocationSensorService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class InstrumentsViewModel_Factory implements Factory<InstrumentsViewModel> {
  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  private final Provider<LocationSensorService> locationSensorServiceProvider;

  public InstrumentsViewModel_Factory(
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<LocationSensorService> locationSensorServiceProvider) {
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
    this.locationSensorServiceProvider = locationSensorServiceProvider;
  }

  @Override
  public InstrumentsViewModel get() {
    return newInstance(preferencesRepositoryProvider.get(), locationSensorServiceProvider.get());
  }

  public static InstrumentsViewModel_Factory create(
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<LocationSensorService> locationSensorServiceProvider) {
    return new InstrumentsViewModel_Factory(preferencesRepositoryProvider, locationSensorServiceProvider);
  }

  public static InstrumentsViewModel newInstance(UserPreferencesRepository preferencesRepository,
      LocationSensorService locationSensorService) {
    return new InstrumentsViewModel(preferencesRepository, locationSensorService);
  }
}
