package com.happyclaw.hikinghappy.ui.screens.instruments;

import android.content.Context;
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
public final class InstrumentsViewModel_Factory implements Factory<InstrumentsViewModel> {
  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  private final Provider<LocationSensorService> locationSensorServiceProvider;

  private final Provider<TrackRepository> trackRepositoryProvider;

  private final Provider<Context> contextProvider;

  public InstrumentsViewModel_Factory(
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<LocationSensorService> locationSensorServiceProvider,
      Provider<TrackRepository> trackRepositoryProvider, Provider<Context> contextProvider) {
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
    this.locationSensorServiceProvider = locationSensorServiceProvider;
    this.trackRepositoryProvider = trackRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public InstrumentsViewModel get() {
    return newInstance(preferencesRepositoryProvider.get(), locationSensorServiceProvider.get(), trackRepositoryProvider.get(), contextProvider.get());
  }

  public static InstrumentsViewModel_Factory create(
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<LocationSensorService> locationSensorServiceProvider,
      Provider<TrackRepository> trackRepositoryProvider, Provider<Context> contextProvider) {
    return new InstrumentsViewModel_Factory(preferencesRepositoryProvider, locationSensorServiceProvider, trackRepositoryProvider, contextProvider);
  }

  public static InstrumentsViewModel newInstance(UserPreferencesRepository preferencesRepository,
      LocationSensorService locationSensorService, TrackRepository trackRepository,
      Context context) {
    return new InstrumentsViewModel(preferencesRepository, locationSensorService, trackRepository, context);
  }
}
