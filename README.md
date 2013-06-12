app-dofollow
============
Do & FOLLOW APP

This an implementation of a BPM software in the Speech programming language. The purpose of this implementation is simply to illustrate how the functional requirements of typical Web 2.0 apps can be programmed in Speech. Thus, we abstract away from non-functional concerns relating to the persistence, presentation and web layers, and focus instead on the business logic of a BPM. You can find explanations on the design of this app in the Speech user guide. Currently, the implementation is far from being complete, but it will eventually cover the 100% of these requirements. We promise!

## Run Online

Follow the instructions on the [apps](http://speechlang.org/apps.php)
section from speechlang.org.

## Compilation

To compile the Do & Follow source code simply follow these steps:

#### Download Do & Follow

To download these sources, you must obtain git and clone the app-dofollow repository.

```shell 
> git clone http://github.com/hablapps/app-dofollow <dofollow>
```

#### Install scala

Speech is implemented as an embedded DSL in Scala, so you must download it first. Follow the instructions at http://scala-lang.org.

#### Install sbt

The app-dofollow project is configured with the sbt build tool. To install sbt follow the instructions at https://github.com/sbt/sbt.

#### Install speech

Download the Speech interpreter from the following address: http://speechlang.org. Then, simply create a <twitter>/lib directory, and install there the speech.jar archive.

#### Compile dofollow

```shell
$ cd <dofollow>
$ sbt 
> test:compile
```

## Run dofollow

From sbt, you can run some tests with the test-only command:

```shell 
> test-only org.hablapps.dofollow.test.All_Speech
```

## License

This software is released under Apache License, Version 2.0.
