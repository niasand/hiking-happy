package com.happyclaw.hikinghappy.ui.screens.instruments;

import com.happyclaw.hikinghappy.data.repository.TrackRepository;
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

  private final Provider<TrackRepository> trackRepositoryProvider;

  public InstrumentsViewModel_Factory(
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<LocationSensorService> locationSensorServiceProvider,
      Provider<TrackRepository> trackRepositoryProvider) {
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
    this.locationSensorServiceProvider = locationSensorServiceProvider;
    this.trackRepositoryProvider = trackRepositoryProvider;
  }

  @Override
  public InstrumentsViewModel get() {
    return newInstance(preferencesRepositoryProvider.get(), locationSensorServiceProvider.get(), trackRepositoryProvider.get());
  }

  public static InstrumentsViewModel_Factory create(
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<LocationSensorService> locationSensorServiceProvider,
      Provider<TrackRepository> trackRepositoryProvider) {
    return new InstrumentsViewModel_Factory(preferencesRepositoryProvider, locationSensorServiceProvider, trackRepositoryProvider);
  }

  public static InstrumentsViewModel newInstance(UserPreferencesRepository preferencesRepository,
      LocationSensorService locationSensorService, TrackRepository trackRepository) {
    return new InstrumentsViewModel(preferencesRepository, locationSensorService, trackRepository);
  }
}
