package com.happyclaw.hikinghappy.ui.screens.trends;

import com.happyclaw.hikinghappy.data.repository.ActivityRepository;
import com.happyclaw.hikinghappy.domain.UserPreferencesRepository;
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
public final class TrendsViewModel_Factory implements Factory<TrendsViewModel> {
  private final Provider<ActivityRepository> repositoryProvider;

  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  public TrendsViewModel_Factory(Provider<ActivityRepository> repositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
  }

  @Override
  public TrendsViewModel get() {
    return newInstance(repositoryProvider.get(), preferencesRepositoryProvider.get());
  }

  public static TrendsViewModel_Factory create(Provider<ActivityRepository> repositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    return new TrendsViewModel_Factory(repositoryProvider, preferencesRepositoryProvider);
  }

  public static TrendsViewModel newInstance(ActivityRepository repository,
      UserPreferencesRepository preferencesRepository) {
    return new TrendsViewModel(repository, preferencesRepository);
  }
}
