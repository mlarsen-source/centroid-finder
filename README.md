# centroid-finder

## *DO THIS FIRST* Wave 0: AI Rules 
AI is *NOT ALLOWED* for generating implementations of the classes.
AI is allowed for helping you make test cases.

Don't have it just create the tests mindlessly for you though! Make sure you're actively involved in making the tests.

DO NOT MIX HUMAN AND AI COMMITS.
EVERY COMMIT THAT USES AI MUST START WITH THE COMMIT MESSAGE "AI Used" AND IT MUST ONLY CREATE/ALTER TEST FILES

For this wave, please have each partner make a commit below with their username acknowledging that they understand the rules, according to the following format:

"I, mlarsen-source, understand that AI is ONLY to be used for tests, and that every commit that I use AI for must start with 'AI Used'"

I, dangrabo, understand that AI is ONLY to be used for tests, and that every commit that I use AI for must start with 'AI Used

"I, YOUR_GITHUB_USERNAME, understand that AI is ONLY to be used for tests, and that every commit that I use AI for must start with 'AI Used'"

## Wave 1: Understand
Read through ImageSummaryApp in detail with your partner. Understand what each part does. This will involve looking through and reading ALL of the other classes records and interfaces. This will take a long time, but it is worth it! Do not skimp on this part, you will regret it! Also look at the sampleInput and sampleOutput folders to understand what comes in and what goes out.

As you read through the files, take notes in notes.md to help you and your partner understand. Make frequent commits to your notes.

## Wave 2: Implement DfsBinaryGroupFinder
This class takes in a binary image array and finds the connected groups. It will look very similar in many ways to the explorer problem you did for DFS! You'll need to understand the Group record to do this well.

Consider STARTING with the unit tests. Remember, you can use AI to help with the unit tests but NOT the implementation. Any AI commit must start with the message "AI Used"

MAKE SURE YOU MAKE THOROUGH UNIT TESTS.

## Wave 3: Implement EuclideanColorDistance
Implement EuclideanColorDistance. You may consider adding a helper method for converting a hex int into R, G, and B components.

Again, consider starting with unit tests. You may consider using WolframAlpha to help you get correct expected values.

MAKE SURE YOU MAKE THOROUGH UNIT TESTS.

## Wave 4: Implement DistanceImageBinarizer
To do this you will need to research `java.awt.image.BufferedImage`. In particular, make sure to understand `getRGB` and `setRGB`. When creating a new image, you can use the below to start the instance:

```
new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
```

Note that a lot of this class will be calling methods in BinaryGroupFinder and ColorDistanceFinder!

MAKE SURE YOU MAKE THOROUGH UNIT TESTS. Consider asking the AI to teach you about mocks and fakes in unit testing and how they may be helpful here.

HINT: `getRGB` returns a 32-bit AARRGGBB color (includes alpha channel). However, ColorDistanceFinder expects the colors to come in RRGGBB format (no alpha channel (most significant 8 bits set to 0)). What can you do to make this conversion happen?

## Wave 5: Implement BinarizingImageGroupFinder
This implementation will be relatively short! It will mostly be calling methods in ImageBinarizer and BinaryGroupFinder.

MAKE SURE YOU MAKE THOROUGH UNIT TESTS. Consider asking the AI to teach you about mocks and fakes in unit testing and how they may be helpful here. I recommend NOT using any external library other than JUnit. If the AI wants to use another external library, consider asking it not to and to make stubs instead.

## Wave 6: Validation
To validate your code is working, make sure you're in the centroid-finder directory and run the below command:

```
javac -cp lib/junit-platform-console-standalone-1.12.0.jar src/*.java && java -cp src ImageSummaryApp sampleInput/squares.jpg FFA200 164
```

This will compile your files and run the main method in ImageSummaryApp against the sample image with a target color of orange and a threshold of 164. It should binarized.png and groups.csv which should match the corresponding files in the sampleOutput directory.

Once you have confirmed it is working, clean up your code, make sure it's committed and pushed, and make a PR to submit. Great job!

## Optional Wave 7: Enhancements?
If you want to, you can make a new branch to start experimenting. See if you can come up with a better color distance method (hint: look up perceptual color spaces). See if you can make your code more efficient or mor suited to spotting salamanders! Experiment with other test files. PLEASE MAKE SURE THIS IS IN A SEPARATE BRANCH FROM YOUR SUBMISSION.



## Centroid Finder Video Part I 
Due Friday by 11:59pm Points 40 Submitting a website url
You are allowed to use AI for this assignment
However, you must be prepared to explain any part of your code and package. Do not just copy things without understanding them! Use the AI to help you understand.

## Overall Goal:
The overall goal of this project will be to make your centroid finder project be able to process mp4 videos. When working properly, it will generate a CSV that tracks the largest centroid over time. The first column will be the number of seconds since the beginning of the video, and the second and third columns will be the x and y coordinates of the largest found centroid at that time. If no centroid is found at that timestamp, coordinates of (-1, -1) should be used. In this first part, we will be restructuring your centroid finder to work with Maven and choose a video processing library. We will handle the actual video processing in a later assignment.

## Restructuring
Have one partner make a branch of your centroid-finder project called "video". Then, switch to that branch. Make any small commit changing the README and then push it up. Have all the other partners pull it down and switch to that branch.
Reorganize your code so that it takes the structure of a Maven project. There's a lot you'll need to move around, and new files you'll need to create. Use the mavenValidate repo as an example. Make sure to properly separate your tests, and make sure that ImageSummaryApp is set as the main class in the pom.
If everything is working properly, you should be able to run mvn exec:java -Dexec.args="sampleInput/squares.jpg FFA200 164" and have the proper files generated. You should also be able to run mvn test to run the tests from the command line.

## Experimenting
Research what video libraries exist in Java. Do your best to understand the pros and cons of them. Search online, talk with your partner, and get input from the AI. Make a new file "options.md" and in it write list of at least three options. IN YOUR OWN WORDS write a few pros and cons for each.
Select one to experiment with. Add it as a dependency in your POM, and make a new class to play around with it. See what it's like to get metadata, and how you extract frame data out of it. Using a small sample video, experiment! Consider switching the main class in your POM to this new experimenting class.
If you are satisfied with this library, you can use it for your project. If it feels difficult to work with or unsuited to this application, choose another one off your list and experiment with it. Keep going until you find a library you like.
Begin thinking about how you can integrate this library with your existing code. In future parts to this project, you will perform that integration.

## Submission
Make sure all your changes (including your experiments with the new video library) are pushed to your video branch. Make a NEW PR from the video branch. Copy the URL of the PR here to submit.
