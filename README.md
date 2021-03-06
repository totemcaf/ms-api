# mw-api Minesweeper

This project is the author proposal as response for the https://github.com/deviget/minesweeper-API 
code challenge.

The project will be developed in Scala due to the position is for a Scala software engineer.

The game fill follow rules described for [Minesweeper](https://en.wikipedia.org/wiki/Minesweeper_(video_game))

The project has a companion [Simple UI](https://github.com/totemcaf/ms-client) that contains a simple API client
(written in Javascript) and a very simple UI.

The UI is deployed in AWS and can be found at http://ec2-3-87-195-146.compute-1.amazonaws.com:5000,
also this API can be riched at http://ec2-3-87-195-146.compute-1.amazonaws.com:8080.

# Public API

See [API description](src/main/resources/swagger.yaml) for the list of endpoints that are available. 

# Design decisions

Code followed a [Clean Architecture](https://engineering.etermax.com/clean-microservice-architecture-in-practice-63051aeb016b),
the intention is to let the business logic be independent on technical decisions.
   
A simple HTTP server was chosen mainly for its simplicity in this toy application.

Some field have refined types, that helps in domain value checks, nad provides better description. 
It is developed with the help of the [Refined](https://github.com/fthomas/refined) library.

# Road map

* [x] Create empty board of size n * m
* [x] Add mines to board
* [x] Let flag square
* [x] Let unflag square
* [x] Let uncover square 
* [x] Explode (and end game) if try to show square with a mine
* [x] Let flag with Question mark or Red flag
* [x] Expose first endpoints
* [x] Let show board status (Hidden, Marked cell, Empty cell, 1 to 8 adjacent mines, Exploded mine, Shown mine)
* [X] Add rest of endpoints
* [X] Add parameter validations
* [X] Allow to handle several games
* [X] Uncover neighbors of uncovered cell without adjacent mines 
* [X] Allow to handle several users/accounts

## Pending, not done

Due to time restrictions the following features were not done.

* [ ] Allow to persiste in external service
* [ ] When adding mines to board, omit the square requested (first square revealed protection)
* [ ] Configure CORS correctly
* [ ] Handle time to finish

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

# To run the application locally

    sbt run
    
# To deploy application

First you need to build the application, use:

    sbt assembly

This will generate a fat jar in the target folder (inside the scala version folder).

In `deploy` folder a helper script let you deploy the app to an instance machine that should have docker installed.

    cd deploy
    bash deploy.sh
    
Before running the script set the environment variable `PEM` to the full path of the PEM file to access the instance.

The instance should have the SSH port open for the IP your are working (see the instance Security Group). 

# References

* https://en.wikipedia.org/wiki/Minesweeper_(video_game)
* https://medium.com/@Methrat0n/wtf-is-refined-5008eb233194
