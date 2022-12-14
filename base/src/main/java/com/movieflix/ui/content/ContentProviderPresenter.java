package com.movieflix.ui.content;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

import com.movieflix.base.prefs.Prefs;
import com.movieflix.model.content.IContentProvider;
import com.movieflix.model.content.IContentUseCase;
import com.movieflix.model.filter.IFilter;
import com.movieflix.mvp.Presenter;
import com.movieflix.mvp.ValueViewState;

public final class ContentProviderPresenter extends Presenter<IContentProviderView> implements IContentProviderPresenter, SharedPreferences.OnSharedPreferenceChangeListener {

    private final IContentUseCase contentUseCase;

    private ContentProviderViewState contentProviderViewState;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public ContentProviderPresenter(@NonNull IContentUseCase contentUseCase) {
        this.contentUseCase = contentUseCase;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        contentProviderViewState = new ContentProviderViewState(
                ContentProviderPresenter.this,
                contentUseCase.getContentProviders(),
                contentUseCase.getContentProvider()
        );
        disposables.add(contentUseCase.getContentProviderObservable().subscribe(new Consumer<IContentProvider>() {

            @Override
            public void accept(@io.reactivex.annotations.NonNull IContentProvider contentProvider) throws Exception {
                contentProviderViewState.apply(contentProvider);
            }
        }));
        disposables.add(contentUseCase.getFilterCheckedObservable().subscribe(new Consumer<IFilter>() {

            @Override
            public void accept(@io.reactivex.annotations.NonNull IFilter filter) throws Exception {
                apply(new ValueViewState<IContentProviderView, IFilter>(filter) {

                    @Override
                    protected void apply(@NonNull IContentProviderView view, @NonNull IFilter filter) {
                        view.onContentFilterChecked(filter);
                    }
                });
            }
        }));
        Prefs.getPopcornPrefs().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onAttach(@NonNull IContentProviderView view) {
        super.onAttach(view);
        contentProviderViewState.apply(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
        contentProviderViewState = null;
        Prefs.getPopcornPrefs().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void setContentProvider(@NonNull IContentProvider contentProvider) {
        contentUseCase.setContentProvider(contentProvider);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
