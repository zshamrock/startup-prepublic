## Thu 26 Nov, 2015

### Language
- *Clojure*
- *Alternatives*: yes, this could be implemented using any language, but I explicitly decided to use Clojure, to see it in action, it turned out to be a good choice, as it made simpler to implement concurrent access and using core.async channels to implement async processing.
- *Risks*: the only concern I have in the moment (as it is true for any dynamic language, but personally it feels that Clojure is somehow different), how easy it would be to understand the code in the future, and maintain and extend it. So far, I am very positive and happy with it. Using smaller functions and good names help to understand and maintain code easier, but it is not specific to Clojure, but to any language.

### Datastore
- *Git*
- *Alternatives*: yes, it is an unusual choice, I consider this as an experiment. But there are reasons for using Git instead classic database. I wanted to minimize the overall operational cost, in the sense of maintaining the data safety. With typical database (either relational or NoSQL, some kind of data replication is required to keep the data safe, which means at least 2 server to maintain, so extra cost as well, small, but still), I wanted to get it done as soon as possible, so relying on Github or Bibucket as the data storage was an interesting idea. Plus, one of the benefit that it is easy to configure Slack to receive notification anytime new push to the repository is done, which was also the goal to be always up to date about how the marketing is doing.
- *Risks*: manual implementation of the concurrent access to the file should be implemented, so the overall solution might be a little bit complex (but Clojure has helped there a lot), and there is a risk of abuse the store, so some extra checks were implemented as well.
