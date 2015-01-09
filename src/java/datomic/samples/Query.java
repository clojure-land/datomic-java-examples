package datomic.samples;

import datomic.Connection;
import datomic.Database;
import datomic.Peer;
import datomic.Util;

import java.util.Scanner;
import java.util.Collection;

import static datomic.Util.read;
import static datomic.samples.Fns.scratchConnection;
import static datomic.samples.Schema.CARDINALITY_MANY;
import static datomic.samples.Schema.CARDINALITY_ONE;
import static datomic.samples.Schema.cardinality;

public class Query {

    public static void main(String[] args) {

        // Your connection information for local mbrainz 1968-1973 subset goes here.
        String uri = "datomic:free://localhost:4334/mbrainz-1968-1973";

        // Connect to transactor, get latest database value from storage.
        Connection conn = Peer.connect(uri);
        Database db = conn.db();

        System.out.println("All releases in the database with release names.");
        Collection results = Peer.query("[:find ?release-name :where [_ :release/name ?release-name]]", db);
        System.out.println(results);
        pause();

        System.out.println("Same as previous query, but with explicit db-var ($).");
        results = Peer.query("[:find ?release-name :in $ :where [$ _ :release/name ?release-name]]", db);
        System.out.println(results);
        pause();

        System.out.println("Query that finds releases by artist. Parameterized to accept artist name as an argument.");
        results = Peer.query("[:find ?release-name " +
                              ":in $ ?artist-name " +
                              ":where [?artist :artist/name ?artist-name] " +
                                     "[?release :release/artists ?artist] " +
                                     "[?release :release/name ?release-name]]",
                            db, "John Lennon");
        System.out.println(results);
        pause();

        System.out.println("This query accepts a relation argument as a vector and destructures it within the :in.");
        results = Peer.query("[:find ?release " +
                             " :in $ [?artist-name ?release-name] " +
                             " :where [?artist :artist/name ?artist-name] " +
                             "        [?release :release/artists ?artist] " +
                             "        [?release :release/name ?release-name]]",
                             db, Util.list("John Lennon", "Mind Games"));
        System.out.println(results);
        pause();


        System.out.println("This query accepts a list of artist arguments and uses destructuring to satisfy the " +
                           "query for each of them (union of results).");
        results = Peer.query("[:find ?release-name " +
                              ":in $ [?artist-name ...] " +
                              ":where [?artist :artist/name ?artist-name] " +
                              "       [?release :release/artists ?artist] " +
                              "       [?release :release/name ?release-name]]",
                              db, Util.list("Paul McCartney", "George Harrison"));
        pause();
                              

        System.out.println("Total number of artists without :artist/country attribute.");
        Integer res = Peer.query("[:find (count ?eid) . " +
                                  ":where [?eid :artist/name] " +
                                         "(not [?eid :artist/country])]",
                                 db);
        System.out.println(res);
        pause();

        System.out.println("Number of artists missing either country or gender. "+
                           "(negation of conjunction reads 'not (clause1 and clause2)'");
        res = Peer.query("[:find (count ?eid) . " +
                          ":where [?eid :artist/name] " +
                                 "(not [?eid :artist/country] " +
                                      "[?eid :artist/gender])]",
                         db);
        System.out.println(res);
        pause();

    }

    private static final Scanner scanner = new Scanner(System.in);

    private static void pause() {
        if (System.getProperty("NOPAUSE") == null) {
            System.out.println("\nPress enter to continue...");
            scanner.nextLine();
        }
    }
}
