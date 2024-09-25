<#import "base.ftl" as base>

<@base.layout>
    You have been invited to join the Jaqpot organization:
    <b>${orgName}</b>. Please note that this invitation will expire in one week
    <br><br>
    Upon accepting the invitation, you will have the ability to view and execute all the models shared with that organization.
    <br><br>
    <a href="${actionUrl}">${actionText}</a>
    <br><br>
</@base.layout>
