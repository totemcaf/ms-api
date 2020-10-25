# mw-api

This project is the author proposal as response for the https://github.com/deviget/minesweeper-API 
code challenge.

The project will be developed in Scala due to the position is for a Scala software engineer.

Code will be follow a [Clean Architecture](https://engineering.etermax.com/clean-microservice-architecture-in-practice-63051aeb016b),
the intention is to let the business logic be independent on technical decisions.
   
The game fill follow rules described for [Minesweeper](https://en.wikipedia.org/wiki/Minesweeper_(video_game))


Features:

* [X] Create empty board of size n * m
* [X] Add mines to board
* [X] Let flag square
* [ ] Let unflag square
* [ ] Let uncover square 
* [ ] Explode if try to show square with mine
* [ ] Let show board status (Hidden, Marked cell, Empty cell, 1 to 8 adjacent mines, Exploded mine, Shown mine)
* [ ] When adding mines to board, omit the square requested (first square revealed protection)


# References

* https://medium.com/@Methrat0n/wtf-is-refined-5008eb233194
