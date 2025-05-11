package com.sivalabs.ft.features.domain.feature;

import com.sivalabs.ft.features.domain.release.Release;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.ScoringFunction;
import org.springframework.data.domain.SearchResult;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface FeatureRepository extends ListCrudRepository<Feature, Long> {
    @EntityGraph(attributePaths = {"product"})
    Optional<Feature> findByCode(String code);

    List<Feature> findByReleaseCode(String releaseCode);

    @Modifying
    void deleteByCode(String code);

    @Modifying
    @Query("delete from Feature f where f.release.code = :code")
    void deleteByReleaseCode(String code);

    boolean existsByCode(String code);

    SearchResult<Feature> findByReleaseNear(Release release, ScoringFunction function);
}
