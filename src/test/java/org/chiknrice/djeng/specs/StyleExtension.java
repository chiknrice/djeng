/*
 * Copyright (c) 2016 Ian Bondoc
 *
 * This file is part of Djeng
 *
 * Djeng is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or(at your option) any later version.
 *
 * Djeng is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.chiknrice.djeng.specs;

import org.concordion.api.Element;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.listener.SpecificationProcessingEvent;
import org.concordion.api.listener.SpecificationProcessingListener;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class StyleExtension implements ConcordionExtension, SpecificationProcessingListener {

    @Override
    public void addTo(ConcordionExtender concordionExtender) {
        concordionExtender.withSpecificationProcessingListener(this);
    }

    @Override
    public void beforeProcessingSpecification(SpecificationProcessingEvent event) {
        System.out.println(event);
    }

    @Override
    public void afterProcessingSpecification(final SpecificationProcessingEvent event) {
        Element head = event.getRootElement().getFirstChildElement("head");
        // Remove original style
        Element[] styles = head.getChildElements("style");
        for (Element style : styles) {
            if (style.getText().contains(".example")) {
                head.removeChild(style);
            }
        }

        // Add css links
        Element css = new Element("link");
        css.addAttribute("type", "text/css").addAttribute("rel", "stylesheet").addAttribute("href", "http://chiknrice.github.io/djeng/stylesheets/styles.css");
        head.appendChild(css);
        css = new Element("link");
        css.addAttribute("type", "text/css").addAttribute("rel", "stylesheet").addAttribute("href", "http://chiknrice.github.io/djeng/stylesheets/github-dark.css");
        head.appendChild(css);

        // Create required structure
        Element wrapper = new Element("div");
        wrapper.addStyleClass("wrapper");
        Element section = new Element("section");
        wrapper.appendChild(section);

        // Modify body
        Element body = event.getRootElement().getFirstChildElement("body");
        if (body != null) {
            Element[] divs = body.getChildElements();
            for (Element child : divs) {
                body.removeChild(child);

                if ("title".equals(child.getAttributeValue("id"))) {
                    child.appendChild(new Element("hr"));
                    Element span = new Element("span").addAttribute("class", "credits left");
                    Element link = new Element("a").addAttribute("href", "https://github.com/chiknrice");
                    link.appendText("chiknrice");
                    span.appendText("Project maintained by ");
                    span.appendChild(link);
                    child.appendChild(span);

                    span = new Element("span").addAttribute("class", "credits right");
                    link = new Element("a").addAttribute("href", "https://twitter.com/michigangraham");
                    link.appendText("mattgraham");
                    span.appendText("Theme by ");
                    span.appendChild(link);
                    child.appendChild(span);

                    section.appendChild(child);
                } else if ("breadcrumbs".equals(child.getAttributeValue("class"))) {
                    body.getElementById("title").prependChild(child);
                }

                // Remove time and concordion link style
                else if ("footer".equals(child.getAttributeValue("class"))) {
                    child.addStyleClass("right");
                    Element timeDiv = child.getFirstChildElement("div");
                    child.removeChild(timeDiv);
                    Element concordionLink = child.getFirstChildElement("a");
                    concordionLink.removeAttribute("style");
                    wrapper.appendChild(child);
                } else {
                    // Restructure body - move elements from body to section
                    section.appendChild(child);
                }

            }
        }

        // Created header div and children
        Element header = new Element("div").addAttribute("id", "header");
        Element nav = new Element("nav");
        header.appendChild(nav);

        Element li = new Element("li").addAttribute("class", "fork");
        Element a = new Element("a").addAttribute("href", "https://github.com/chiknrice/djeng").appendText("View On GitHub");
        li.appendChild(a);
        nav.appendChild(li);

        li = new Element("li").addAttribute("class", "downloads");
        a = new Element("a").addAttribute("href", "https://bintray.com/chiknrice/maven/djeng/_latestVersion").appendText("JAR");
        li.appendChild(a);
        nav.appendChild(li);

        li = new Element("li").addAttribute("class", "title").appendText("DOWNLOADS");
        nav.appendChild(li);

        // Append the newly created header and wrapper
        body.appendChild(header);
        body.appendChild(wrapper);
    }

}
