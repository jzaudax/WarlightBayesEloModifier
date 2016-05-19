See https://www.warlight.net/wiki/Ladder_Ranks_and_Ratings
See www.remi-coulom.fr/Bayesian-Elo/
The program requires java to be installed and included on the PATH.

Modify run/settings.txt to add games, remove games, and switch the game winners
To run the script, doubleclick run_modifier.cmd.
The results file with rankings and ratings will be created once it is done running (file is called results_???.txt).

Notes:
The program will rate and rank all players, including those who aren't participating in the ladder or have fewer than 20 games.

---------------- EXAMPLE run/settings.txt files ----------------
The following is an example settings.txt file that reads directly from the warlight servers (retrieves the 1v1 ladder games):
inputfile:http://data.warlight.net/Data/BayeseloLog0.txt
outputfile:ModifiedBayeseloLog0.txt
mode:fast
removegame:Master Jz::Krzychu
togglewin:Master Jz::MIFRAN
addgame:MasterJz::Pushover::loss

The following is a settings.txt file that reads from a file on the local hard drive:
inputfile:BayeseloLog0.txt
outputfile:ModifiedBayeseloLog0.txt
mode:pretty
removegame:Master Jz::Krzychu
removegame:Master Jz::Master of the Dead
togglewin:Master Jz::MIFRAN
addgame:MasterJz::Pushover::loss

---------------- Description of parameters in settings.txt ----------------
inputfile
The file containing the log of wins and losses. This can be a local file or a URL.
1v1 URL: http://data.warlight.net/Data/BayeseloLog0.txt
2v2 URL: http://data.warlight.net/Data/BayeseloLog1.txt
Seasonal Ladder: http://data.warlight.net/Data/BayeseloLog4000.txt
(4000 is season 1, 4001 is season 2, etc)

outputfile
The modified version of the input file with the changed games (the program creates this file)

mode
The options are "fast" and "pretty". There is no functional difference. The output for pretty looks
nicer but the algorithm takes appriximately 3 times as much time to run.

removegame
Remove a game. The format is "removegame:Player1::Player2". The order of the players doesn't matter.

addgame
Add a game. The format is "addgame:Player1::Player2". The order of the players doesn't matter.

togglewin
Switch the winner of the game. The format is "removegame:Player1::Player2". The order of the players doesn't matter.

pastetype
There are three options allowed for this, "removegame", "addgame", and "togglewin". Include this parameter, 
and then highlight and copy ladder games and paste them on the following lines. This is useful for adding
several games at once. 

Here's an example of what it might look like (this may not work on all browsers and may stop working after a warlight update):
pastetype:removegame
HotBeachBum defeated Master Jz 	10874936 	4/8/2016 15:31:14
Pushover defeated Master Jz 	10108439 	1/10/2016 17:35:31
Master Jz defeated Ender2010 	10004911 	12/21/2015 05:07:58
Master Jz defeated Peixoto 	9991662 	12/18/2015 01:55: