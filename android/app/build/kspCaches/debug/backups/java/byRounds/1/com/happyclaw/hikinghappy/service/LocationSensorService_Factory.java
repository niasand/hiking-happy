package com.happyclaw.hikinghappy.service;

import android.content.Context;
import com.google.android.gms.location.FusedLocationProviderClient;
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
public final class LocationSensorService_Factory implements Factory<LocationSensorService> {
  private final Provider<Context> contextProvider;

  private final Provider<FusedLocationProviderClient> fusedLocationClientProvider;

  public LocationSensorService_Factory(Provider<Context> contextProvider,
      Provider<FusedLocationProviderClient> fusedLocationClientProvider) {
    this.contextProvider = contextProvider;
    this.fusedLocationClientProvider = fusedLocationClientProvider;
  }

  @Override
  public LocationSensorService get() {
    return newInstance(contextProvider.get(), fusedLocationClientProvider.get());
  }

  public static LocationSensorService_Factory create(Provider<Context> contextProvider,
      Provider<FusedLocationProviderClient> fusedLocationClientProvider) {
    return new LocationSensorService_Factory(contextProvider, fusedLocationClientProvider);
  }

  public static LocationSensorService newInstance(Context context,
      FusedLocationProviderClient fusedLocationClient) {
    return new LocationSensorService(context, fusedLocationClient);
  }
}
