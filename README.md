# RssToTwitter

Twitter API is gone and this project uses browser automation (selenium) to post tweets.\
This is a console application, no GUI.\
You need to enter your twitter username and password once, they will be saved in plain text file.\
Google Chrome must be installed on the running machine\
(Linux -> Download and install google-chrome-stable_current_amd64.deb)\
(Windows -> install Google Chrome)\
Do not worry chrome will run headless.\
\
Sample code reads BBC News feed then posts the news title with the link.\
Last published article's date is saved to prevent double posts.
\
You can use crontab to run this example every n minutes.

# To Do
Some css & xpath selectors use Turkish keywords, these selector expressions could be read from properties file for other languages
