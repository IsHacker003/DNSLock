# DNSLock
I made this app as a solution to the following question that I asked on Android Enthusiasists StackExchange:
> Despite repeated efforts to explain them, some family members keep turning off the private DNS setting in Android, **which leads to the invasion of their phones by ads, trackers and malware.** So, I want to lock down the private DNS setting so it can't be changed or turned off, and I need to do it **without using root or third-party apps.** The problem with third-party apps is that they will see the app on the launcher or in Settings > App and just uninstall it to bypass the block. And if I set the DNS on the WiFi router itself, they will refuse to use the WiFi and use cellular network instead.
>
> I'm desperate, **is there any solution?**

and also to [this](https://android.stackexchange.com/questions/264946/amend-private-dns-and-block-it-to-end-user) question asked by another user.

## So what does it do?
First, instead of actually locking the Private DNS setting (which would require modification of the Settings app, which in turn requires root), this app runs **every minute** and sets the Private DNS to a hostname you specify! So, even if Private DNS has been turned off, it will be turned on again by this app and set to the desired hostname, just a minute later. The app itself does NOT run in the background to do this; It uses AlarmManager to schedule starting of a BroadcastReiver every minute, which means the app will continue to function even if it has been killed!

Second, the app is disguised under a convincing name ("Android System Services") and has no launcher icon, which makes it less likely to become a suspect for getting uninstalled. The app itself is also only a few hundred KBs in size when installed, just like most other system apps.
