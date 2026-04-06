package com.happyclaw.hikinghappy.di;

import com.happyclaw.hikinghappy.service.LocationSensorService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class ServiceModule_ProvideLocationSensorServiceFactory implements Factory<LocationSensorService> {
  private final Provider<LocationSensorService> serviceProvider;

  public ServiceModule_ProvideLocationSensorServiceFactory(
      Provider<LocationSensorService> serviceProvider) {
    this.serviceProvider = serviceProvider;
  }

  @Override
  public LocationSensorService get() {
    return provideLocationSensorService(serviceProvider.get());
  }

  public static ServiceModule_ProvideLocationSensorServiceFactory create(
      Provider<LocationSensorService> serviceProvider) {
    return new ServiceModule_ProvideLocationSensorServiceFactory(serviceProvider);
  }

  public static LocationSensorService provideLocationSensorService(LocationSensorService service) {
    return Preconditions.checkNotNullFromProvides(ServiceModule.INSTANCE.provideLocationSensorService(service));
  }
}
