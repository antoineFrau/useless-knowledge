# Pay your knowledge
> Android project - Get data from API, and cache it on json file.


## Explain how does it work
This project using HandlerThread, ViewPager, HTTP Request, Checking Internet status.

At the first launch of the app it will try to get data from API, and save it on the JSON cache file. If the user is not connected to internet we will check if there is cached data. If there is no data, we informe user, else we show him cached data.

When we detect Internet status is up, we notify user with snackbar and tell him to refresh the list.

The API HTTP request is done in an HandlerThread and it will notify UIThread when finish.

## Author
Antoine Frau - Master 1 Full-Stack Developer.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.