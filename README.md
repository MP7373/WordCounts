# WordCounts

A simple program that takes a user provided url and scrapes the top 25 most frequently occurring words from that page.

Some assumptions made are that the page is English and that words with apostrophes, hyphens, etc. are not considered different than words not containing those characters.

The top 25 most frequently occurring words themselves are not sorted, it is only guaranteed that the top 25 words are the top 25 most frequently occurring. In the case of occurrence frequency ties no secondary sort criteria is used so in cases of many words occurring the exact same amount of times which words end up in the top 25 is not predictable.   