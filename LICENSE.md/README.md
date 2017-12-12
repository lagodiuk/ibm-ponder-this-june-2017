# ibm-ponder-this-june-2017
This repository contains my solution of the IBM Ponder This challenge in June 2017 (organized by IBM Research).

## Original problem statement
This month's challenge involves a game that can be played with seven-digit numbers (leading zeroes are allowed), such as the serial number of a bus ticket, a license plate number in Israel, phone numbers, etc. 

To play the game, take the seven-digit number and place the four basic operations `(+, -, *, and /)` and parentheses around and between the digits, using each digit exactly once (the parentheses and operations can be used more than once). Without changing the order or concatenating two or more digits into a larger number, use the basic operations and parentheses so that the answer of the mathematical equation is exactly 100. 

Some seven-digit numbers are solvable, like 3141592 which can be solved as `(3*1+4)*(1*5+9)+2` or a single digit change of it 3146592, which can also be solved as `3*((-1)+(-4)/6+5*(9-2))`, while other seven-digit numbers simply cannot be solved, such as 0314157. 

There are 63 ways to change a single digit in a seven-digit number. 
Here's the challenge: Find a seven-digit number that is not solvable, but is made solvable by 62 of the 63 single-digit changes. 

**Bonus '*' if the number is prime.**

*Update (5/6):* Division by zero is not allowed, but you can use non integers and unary minus (like the example above); and you get a '*' for every prime solution.

The URL of the web page with the problem description: http://www.research.ibm.com/haifa/ponderthis/challenges/June2017.html
