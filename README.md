# DiscordAlertBot
This is a simple bot that allows users to keep up to date with their favorite [Twitch](https://twitch.tv) streamers.
Users can select the streamers they would like to be notified of. When those selected streamers go live or offline, embedded alerts are sent to the preselected Discord text channels that the user had earlier specified.

---

## Discord Bot Link
[**Twitch Stream Alerts**](https://discord.com/api/oauth2/authorize?client_id=1013310489964580966&permissions=139586881600&scope=bot%20applications.commands)
<img src="https://user-images.githubusercontent.com/41808571/187093008-929237d8-3773-440a-8061-9cb4f8f9bf0a.png" width="15" height="15"/>

---

## Slash Commands
Slash commands allow users to access the list of the available functions the bot has to offer.

#### Available Commands

/ | Description
--- | --- 
`/addchannel` | Type in a text channel name to receive alerts there
`/addstreamer` | Type in a Twitch.tv streamer name to receive their alerts
`/displaychannels` | Get a list of all channels on notification list
`/displaystreamers` | Get a list of all streamers on the streamer list
`/removechannel` | Type in a channelname to stop getting alerts there
`/removestreamer` | Type in a streamer name to stop receiving their alerts


[JDA-image]:https://raw.githubusercontent.com/DV8FromTheWorld/JDA/assets/assets/readme/logo.png
[JDA-link]:https://github.com/DV8FromTheWorld/JDA#download
[Twitch4J-image]:data:image/png;base64,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
[Twitch4J-link]:https://twitch4j.github.io/
