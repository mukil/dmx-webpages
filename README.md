
DeepaMehta 4 Pages 
##################

This DeepaMehta 4 Plugin brings really simple web-publishing capabilities directly into your DeepaMehta 4 Webclient.

It builds on our work of last year, especially on the

 * [dm4-thymeleaf](https://github.com/jri/dm4-thymeleaf) module

## Installation Requirements

See [jri/deepamehta](https://github.com/jri/deepamehta/#1-check-requirements)

Additionally you need to install the following module:

 * [dm4-thymeleaf](https://github.com/jri/dm4-thymeleaf) module

You can find both at [http://download.deepamehta.de](http://download.deepamehta.de).

## Usage: Creating a Webpage

To start working a _Webpage_ simply use the `Create` menu in Toolbar (upper grey area of DeepaMehta).

To publish a _Webpage_ it must be connected to a _Website_.

1. Navigate and reveal your **Website** (e.g. via following the `My Website` button after clicking your `Username` in the `Toolbar`). The website topic allows you to edit and enter some basic information which occurs on all your webpages (Footer, About, Layout, etc):

You can also reveal your website through search `By Type` and then selecting `Website`.

2. Create a _Webpage_

 * _Create_ -> _Webpage_ and _Edit_ it, for example with a
 * Headline -> Title of this webpage
 * Content -> The main content area of your webpage
 * Web Alias -> This must be an URI compliant name for your resource
 * What is this page about? -> The meta=description for your webpage as well as the summary shown on your frontpage. Used by search engines to index your webpage.
 * Author Name -> Name of the content authors (or contributors).
 * Draft?-> A checkbox, if checked your webpage content will only be published to logged in users who have access permissions on the topic.

3. Associate the _Webpage_ with your _Website_

 * Either: Right click on the _Website_ or the _Webpage_ and select the  _Associate_ command
 * Or: Drag & Drop the line (=Association) over the other (_Website_ or _Webpage_) topic

That's it. Your webpage is now published under your hostname and its so called _Web Alias_. The very same steps apply if you want to publish a _Redirect_ or a  _Menu item_ on your website.

Note: The permission who can see your published webpage depends on the so called _SharingMode_ of the workspace your webpage is assigned to.


## Changelog

**0.4** -- Upcoming

* Completely revised webpage application model
* Introduced new icons
* Not compatible with previous version (dm4-webpages-0.3)
* Uses Thymeleaf 2.1.3

Note: You cannot upgrade a _dm47-webpages-0.3_ installation to use _dm47-webpages-0.4_.

**0.3** -- Nov 23, 2015

* Useful to create and publish _one_ website
* Compatible with the collaborative DeepaMehta 4.7

**0.1.1** -- Oct 09, 2015

* Simple Web Pages for DeepaMehta 4.4.x

-----------
Malte Reißig<br/>
Copyright 2015
