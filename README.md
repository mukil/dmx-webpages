
# DeepaMehta 4 Webpages

This DeepaMehta 4 Plugin brings simple, _multi-site_ web-publishing capabilities to your DeepaMehta 4 installation. Installing it introduces the types _Webpage_, _Redirect_, _Menu Item_ and _Website_ to your DeepaMehta 4.

![Website Example: Setup of the Kiezatlas Webpages](https://github.com/mukil/dm4-webpages/raw/master/kiezatlas-website-setup-graph-only.png)

Feature-wise it allows for a global standard website and, if desired, this module provides each of your users their personal resource for publishing. Subsequently each website can appear in different style or layout, has its own menu items, footer and frontpage. Webpages can also extend the style or layout of their website and users are also allowd to load their own JavaScript for their page.

The HTML generated by this module tries to map (at best as possible) the DeepaMehta standard types to terms of the [Schema.org](https://schema.org) vocabulary.

It builds on our recent work, especially on the [dm4-thymeleaf](https://github.com/jri/dm4-thymeleaf) module.

## Installation Requirements

See [system requirements at jri/deepamehta](https://github.com/jri/deepamehta/#1-check-requirements)

Additionally you need to install the following module:

 * [dm4-thymeleaf](https://github.com/jri/dm4-thymeleaf) module

You'll find the most recent stable builds of both plugins at [http://download.deepamehta.de](http://download.deepamehta.de).

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

**0.4.1** -- Nov 03, 2016

Pleae have read tjhe commit message for a description of fixes and improvements [here](https://github.com/mukil/dm4-webpages/commit/79ad5ea048d440e780e58022bb51adcba62e18be).

**0.4** -- Aug 09, 2016

* Completely revised webpage application model
* Introduced new icons, types and a specific web-alias renderer
* Depends on dm4-thymeleaf module version 0.6.1
* Not compatible with previous version (dm4-webpages-0.3)
* Developer note: This version only installs migrations 1, 2 and 3.<br/>
  Migration 4 and 5 are still in flux and may instal with the next release.
* Uses Thymeleaf 2.1.3

Note: You cannot upgrade a _dm47-webpages-0.3_ installation to use _dm47-webpages-0.4_.

**0.3** -- Nov 23, 2015

* Useful to create and publish _one_ website
* Compatible with the collaborative DeepaMehta 4.7

**0.1.1** -- Oct 09, 2015

* Simple Web Pages for DeepaMehta 4.4.x

-----------
Malte Reißig<br/>
Copyright 2016
