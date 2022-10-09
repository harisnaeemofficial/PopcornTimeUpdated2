package com.movieflix.ui.details;

import android.support.annotation.NonNull;

import io.reactivex.functions.Consumer;
import com.movieflix.base.model.video.info.Episode;
import com.movieflix.base.model.video.info.Season;
import com.movieflix.base.model.video.info.TvShowsInfo;
import com.movieflix.model.ChoiceProperty;
import com.movieflix.model.content.IContentUseCase;
import com.movieflix.model.details.IDetailsUseCase;

public final class DetailsTvShowPresenter extends DetailsPresenter<TvShowsInfo, IDetailsTvShowView> implements IDetailsTvShowPresenter {

    private final SeasonsViewState seasonsViewState = new SeasonsViewState();
    private final EpisodesViewState episodesViewState = new EpisodesViewState();

    public DetailsTvShowPresenter(@NonNull IContentUseCase contentUseCase, @NonNull IDetailsUseCase detailsUseCase) {
        super(contentUseCase, detailsUseCase);
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        seasonsViewState.apply(detailsUseCase.getSeasonChoiceProperty());
        disposables.add(detailsUseCase.getSeasonChoiceProperty().getObservable().subscribe(new Consumer<ChoiceProperty<Season>>() {

            @Override
            public void accept(@io.reactivex.annotations.NonNull ChoiceProperty<Season> property) throws Exception {
                seasonsViewState.apply(property);
            }
        }));
        episodesViewState.apply(detailsUseCase.getEpisodeChoiceProperty());
        disposables.add(detailsUseCase.getEpisodeChoiceProperty().getObservable().subscribe(new Consumer<ChoiceProperty<Episode>>() {

            @Override
            public void accept(@io.reactivex.annotations.NonNull ChoiceProperty<Episode> property) throws Exception {
                episodesViewState.apply(property);
            }
        }));
    }

    @Override
    protected void onAttach(@NonNull IDetailsTvShowView view) {
        super.onAttach(view);
        seasonsViewState.apply(view);
        episodesViewState.apply(view);
    }

    private final class SeasonsViewState extends ChoicePropertyViewState<Season> {

        @Override
        public void apply(@NonNull IDetailsTvShowView view) {
            view.onSeasons(items, position);
        }
    }

    private final class EpisodesViewState extends ChoicePropertyViewState<Episode> {

        @Override
        public void apply(@NonNull IDetailsTvShowView view) {
            view.onEpisodes(items, position);
        }
    }
}
