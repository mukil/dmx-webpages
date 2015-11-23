
DeepaMehta 4 Pages 
##################

This DeepaMehta 4 Plugin brings really simple web-publishing capabilities directly into your DeepaMehta 4 Webclient.

It builds on our work of last year, especially on the

 * [dm4-webactivator](https://github.com/jri/dm4-webactivator) module

## Installation Requirements

See [jri/deepamehta](https://github.com/jri/deepamehta/#1-check-requirements)

Additionally you need to install the following module:

 * [dm4-webactivator](https://github.com/jri/dm4-webactivator) module

You can find both at [http://download.deepamehta.de](http://download.deepamehta.de).

## Usage

Note: Currently just user "admin" can create a new **Webpage**, **Wepage Redirect** or **Webpage Menu Item**. Though, after moving existing topics to more collaborative workspaces other users should be able to _Edit_ the contents of such.

1. Navigate and reveal the _default_ **Website** topic to edit and enter some website wide informations, like:

 * Website Title
 * Website Caption
 * Website About Info
 * Website Footer
 * Path to Custom CSS File

You can find your website topic through clicking on the _Username_ topic of your User Account _admin_.

2. Create a _Webpage_ topic and publish it.

 * _Create_ -> _Webpage_ and _Edit_ it, for example with a
 * Webpage Title -> Title of this webpage
 * Main Part -> The main content area of your webpage
 * Web Alias -> This must be an URI compliant name for your resource
 * Web Description -> The meta=description for your webpage, used by search engines when indexing your content
 * Author Name -> Enter at least your name
 * Published -> A checkbox, if checked your webpage content will be published.

3. Associate the _Webpage_ topic with your _Website_ topic.

 * Right click on the _Website_ or the _Webpage_ topic and select the  _Associate_ command
 * Drag & Drop the line (=Association) over the other (_Website_ or _Webpage_) topic

That's it. Your webpage is now published under your hostname and its so called _Web Alias_. The very same steps apply if you want to publish a _Redirect_ or a  _Menu item_ on your website.

Note: The permission who can see your published webpage depends on the so called _SharingMode_ your webpage topic currently resides in.


## Changelog

**0.3**, Nov 23, 2015

* Useful to create and publish _one_ website
* Compatible with the collaborative DeepaMehta 4.7

**0.1.1**, Oct 09, 2015

* Simple Web Pages for DeepaMehta 4.4.x

Author:
Malte Reißig, Copyright 2015
