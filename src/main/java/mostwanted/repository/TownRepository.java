package mostwanted.repository;

import mostwanted.domain.entities.Town;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface TownRepository extends JpaRepository<Town, Integer> {

    Optional<Town> findByName(String name);

    @Query(value = "" +
            "select t.name as Name, count(r.id) as Racers " +
            "from most_wanted_db.racers as r " +
            "join most_wanted_db.towns as t " +
            "on r.home_town_id = t.id " +
            "group by Name " +
            "order by Racers desc, Name ",
            nativeQuery = true)
    List<Map<String, String>> findAllByCountOfRacers();

}
