# scallbacks
Demo of strongly typed callbacks in web page (No, really)

Authors: Tom Ballard, Chat GPT, not necessarily in that order.

To Run (if you are adequately java-centric):

> git clone https://github.com/tballard/scallbacks
> sbt run

This is for anyone doing a web app that uses a scala backend with scala.js on the frontend. If you are familiar with the sloth library, you know that it defines a couple of clever macros that allow making calls from the client to the server that are in essence normal function calls. Basically it creates the code at compile time that translates a function call into a request object on the client. The implementer takes care of the transport to the server where another macro has created the code to change it into a function call. The response or an error is transported back and converted to the return value. Well, it's a Future, so it's just sort of a return value.

But wait. You do that, and think it's pretty cool, but you are still putting weakly typed calls in your generated pages, e.g. "onclick = 'MyClass.myFunc(event, 42)'". Does that class exist? That method? Are the arguments legit? Do you have a secret desire to include giant data structures in your callback arguments?

This file demonstrates how one may misuse the sloth library to get compile time checking and generalize your bliss.
