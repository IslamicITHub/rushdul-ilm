package com.rushdululilm.app.viewmodel;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "cast"
})
public final class VideoLibraryViewModel_Factory implements Factory<VideoLibraryViewModel> {
  @Override
  public VideoLibraryViewModel get() {
    return newInstance();
  }

  public static VideoLibraryViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static VideoLibraryViewModel newInstance() {
    return new VideoLibraryViewModel();
  }

  private static final class InstanceHolder {
    private static final VideoLibraryViewModel_Factory INSTANCE = new VideoLibraryViewModel_Factory();
  }
}
