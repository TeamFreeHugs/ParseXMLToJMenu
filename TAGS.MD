#TAGS

Here you find all the tags.

###JMenuBar
This is the main XML tag to show that the document is a JMenuBar.

###JMenu
This is a menu tag.   
You can do `<info></info>` to set the info.
You can also do `<children></children>` to set the menu's children.

###Info
Stores info about the JMenu

###Children
This tag stores the children inside a JMenu. Allowed children:

    <jmenuitem>
    
This is a menu item.

    <group>
    
A group for `<radiobutton>` and `<checkbox>`

###JMenuItem
This is a basic menu item.  
Use `<text>` to set the text displayed.  
Use `<mnemonic>` to set the mnemonic key. Please use the VK_KEY format.  
Use `<accelerator>` to set the accelerator.
Use `<icon>` the set the icon.

###Group
Stores `<radiobutton>` and `<checkbox>`

###Text
Text for the child.

###Selected
Sets the child as selected if true.

###Mnemonic
Sets the mnemonic. It allows for controlling the menu with your keyboard.

###Accelerator
Sets the accelerator.    
Syntax:

    <accelerator>
        <key>VK_S</key>
        <mask>CTRL</mask>
        <mask>ALT</mask>
        <mask>SHIFT</mask>
    </accelerator>

This would mean that pressing <kbd>Control</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>S</kbd> will activate that item.

###Radiobutton
Your everyday radio button. Store in `<group>` to work.

###Checkbox
Your everyday checkbox. Store in `<group>` to work.

###Separator
Just a separator, you know.

#EVENTS

This allow you to control your menu items.

###Events
This stores all the event handlers.

Currently supported event handlers:  

    <click>
    
###Click
Event listener for click events.

If you want to call a class, do something like this:

    <click className="EventHandler" packageName="my.package" methodName="handleClickDemo"/>
    
Or, to run some code:

    <click eval="true" lang="JavaScript" run="print('Hi from JavaScript!\n');"/>
    
    

#CONDITIONS
If, else, for...


###If
If statement. Right now, the following types are supported:

    <if type="os">

This checks for the OS type.

Inside, have whichever of the following tags you want.

    <windows>
    
    <mac>
    
    <linux>
    
    <else>
    
Inside the OS blocks, just use whatever event handlers specific for the OS you have, or whatever class you want to use. 

    <if type="os">
        <windows>
            <click eval="true" lang="JavaScript" run="print('Hi windows\n');"/>
        </windows>
        <mac>
            <click eval="true" lang="AppleScript" run="display notification &quot;Lorem ipsum dolor sit amet&quot; with title &quot;Title&quot;"/>
        </mac>
        <linux>
            <click eval="true" lang="JavaScript" run="print('Hi linux\n');"/>
            <click eval="true" lang="JavaScript" run="print('Hi linux 2.0\n');"/>
            <click eval="true" lang="JavaScript" run="print('Testing third line for linux\n');"/>
            <click eval="true" lang="JavaScript" run="print('And while at it, why not another?\n');"/>
            <click eval="true" lang="JavaScript" run="if (typeof JSON !== 'undefined') print('Also, JSON: ' + JSON.stringify({hello: 'world'}) + '\n');"/>
        </linux>
        <else>
            <click eval="true" lang="JavaScript" run="print('I dunno what you are...');"/>
        </else>
    </if>
    
If it's Windows, it print's 1 line of hello world.

If it's mac, it shows a popup.

If it's linux, it prints more things.

Otherwise, it prints it doesn't know who you are.