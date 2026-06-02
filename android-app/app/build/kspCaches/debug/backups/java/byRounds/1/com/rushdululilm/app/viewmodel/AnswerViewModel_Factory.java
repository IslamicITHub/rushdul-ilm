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
public final class AnswerViewModel_Factory implements Factory<AnswerViewModel> {
  @Override
  public AnswerViewModel get() {
    return newInstance();
  }

  public static AnswerViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AnswerViewModel newInstance() {
    return new AnswerViewModel();
  }

  private static final class InstanceHolder {
    private static final AnswerViewModel_Factory INSTANCE = new AnswerViewModel_Factory();
  }
}
