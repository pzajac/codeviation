/*
 * Attachment.java
 * 
 * Created on Jul 23, 2007, 2:08:20 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.bugtracking.issuezilla;

import java.sql.Timestamp;

/**
 * 
 * <!-- Data pertaining to attachments.  NOTE - some of these fields    -->
 * <!-- are currently unimplemented (ispatch, filename, etc.).          -->
 * 
 * <!ELEMENT attachment (mimetype, attachid, date, desc, ispatch*, filename,
 * submitter_id, data, attachment_iz_url)>
 * 
 *   <!-- encoding   : How the inline attachment is encoded.            -->
 * 
 *   <!ATTLIST attachment encoding CDATA #FIXED "Base64" >
 * 
 *   <!-- mimetype     : Mime type for the attachment.                  -->
 *   <!-- attachid     : A unique id for this attachment.               -->
 *   <!-- date         : Timestamp of when added 'yyyy-mm-dd hh:mm'     -->
 *   <!-- desc         : Short description for attachment.              -->
 *   <!-- ispatch      : Whether attachment is a patch file.            -->
 *   <!-- filename     : Filename of attachment.                        -->
 *   <!-- submitter_id : Issuezilla ID of attachement submitter.        -->
 *   <!-- data         : Encoded attachment.                            -->
 *   <!-- attachment_iz_url : URL to attachment in iz.                  -->
 * 
 *   <!ELEMENT mimetype (#PCDATA)>
 *   <!ELEMENT attachid (#PCDATA)>
 *   <!ELEMENT date (#PCDATA)>  
 *   <!ELEMENT desc (#PCDATA)> 
 *   <!ELEMENT ispatch (#PCDATA)>
 *   <!ELEMENT filename (#PCDATA)>
 *   <!ELEMENT submitter_id (#PCDATA)>
 *   <!ELEMENT data (#PCDATA)>   
 *   <!ELEMENT attachment_iz_url (#PCDATA)>   
 */
public class Attachment {
    
    private String mimetype; 
    private String attachid; 
    private Timestamp date;
    private String desc;
    private String ispatch;
    private String filename;
    private String submitter_id;
    private String data; 
    private String attachment_iz_url;

    public Attachment(String mimetype, String attachid, Timestamp date, String desc, String ispatch, String filename, String submitter_id, String data, String attachment_iz_url) {
        this.mimetype = mimetype;
        this.attachid = attachid;
        this.date = date;
        this.desc = desc;
        this.ispatch = ispatch;
        this.filename = filename;
        this.submitter_id = submitter_id;
        this.data = data;
        this.attachment_iz_url = attachment_iz_url;
    }

    public String getAttachid() {
        return attachid;
    }

    public void setAttachid(String attachid) {
        this.attachid = attachid;
    }

    public String getAttachment_iz_url() {
        return attachment_iz_url;
    }

    public void setAttachment_iz_url(String attachment_iz_url) {
        this.attachment_iz_url = attachment_iz_url;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    
    public byte[] getDecodedData() {
        if (this.data == null) {
            return null;
        }
        return Base64.decode(this.data);
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getIspatch() {
        return ispatch;
    }

    public void setIspatch(String ispatch) {
        this.ispatch = ispatch;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getSubmitter_id() {
        return submitter_id;
    }

    public void setSubmitter_id(String submitter_id) {
        this.submitter_id = submitter_id;
    }
        
     
    
}
