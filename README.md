# galvanize-gazette

This single page app was a website requested by Galvanize for an assessment.  It requires an Postgres SQL database (migrations files are included and sparse).

It's built using mostly ClojureScript, with some custom SQL queries bound to Clojure functions on the backend API.

It uses Reagant which wraps React, along with Clojure's immutable data structures.

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein run

## License

Copyright © 2016 Brett Lyons
