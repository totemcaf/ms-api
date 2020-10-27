# mw-api

This project is the author proposal as response for the https://github.com/deviget/minesweeper-API 
code challenge.

The project will be developed in Scala due to the position is for a Scala software engineer.

The game fill follow rules described for [Minesweeper](https://en.wikipedia.org/wiki/Minesweeper_(video_game))

# Public API

See [API description](src/main/resources/swagger.yaml) for the list of endpoints that are available. 

# Road map

* [x] Create empty board of size n * m
* [x] Add mines to board
* [x] Let flag square
* [x] Let unflag square
* [x] Let uncover square 
* [x] Explode if try to show square with a mine
* [x] Let flag with Question mark or Red flag
* [x] Expose first endpoints
* [x] Let show board status (Hidden, Marked cell, Empty cell, 1 to 8 adjacent mines, Exploded mine, Shown mine)
* [X] Add rest of endpoints
* [X] Add parameter validations
* [X] Allow to handle several games
* [ ] Allow to handle several users/accounts
* [ ] Allow to persiste in external service
* [ ] When adding mines to board, omit the square requested (first square revealed protection)

# Design decisions

Code will be follow a [Clean Architecture](https://engineering.etermax.com/clean-microservice-architecture-in-practice-63051aeb016b),
the intention is to let the business logic be independent on technical decisions.
   
A simple HTTP server was chosen mainly for its simplicity in this toy application.

## Concurrency control

To simplify the development of the project, no provision for concurrency control was provided.
It is possible for two simultaneous requests to operate on the same game and one of the requests will override
the other request result. For a real production application this should be solved.

Note: If time is available this will be solved by adding Optimistic Concurrency control strategy to the repository.

## Database housekeeping

To simplify this exercise no provision for cleaning the database for old games. More requirementes are needed to
correctly design the solution.

# To run test

    sbt test

#To run the application locally

    sbt runMain 
   
# References

* https://medium.com/@Methrat0n/wtf-is-refined-5008eb233194
