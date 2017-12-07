/*
 * Copyright (c) 2017 RockScript.io.
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var converter = new showdown.Converter({tables: 'true'});
var currentView = 'introduction';

// counters to know when the last page was loaded
var markdownPagesToLoad = 0;
var markdownPagesLoaded = 0;

function load(fileName, label, classes) {
  markdownPagesToLoad++;
  let baseUrl = window.location.pathname;
  if (baseUrl.endsWith("index.html")) {
    baseUrl = baseUrl.substring(0, baseUrl.length-10);
  }
  if (!baseUrl.endsWith('/')) {
    baseUrl += '/';
  }

  let id = fileName.substring(0, fileName.length-3);
  let linkHtml = '<a '+
      (classes ? 'class="'+classes+'"' : '')+
      ' onclick="show(\''+id+'\')">'+label+'</a>'
  $('.toc').append(linkHtml);

  $.get(baseUrl+fileName, function(md) {
    let html = '<div id="'+id+'" class="hidden"> \n' +
        '<h1>'+label+'</h1> \n'+
        converter.makeHtml(md)+'\n'+
        '</div>';
    $('.content').append(html);

    // If this was the last page to be loaded
    markdownPagesLoaded++;
    if (markdownPagesToLoad==markdownPagesLoaded) {
      if (window.location.hash && window.location.hash.startsWith('#')) {
        showContent(window.location.hash.substring(1));
      } else {
        showContent('introduction');
      }
      $(document).ready(function() {
        $('pre code').each(function(i, block) {
          hljs.highlightBlock(block);
        });
      });
    }
  });
}

function show(id) {
  showContent(id);
  history.pushState(id, 'Back title '+id, '#'+id);
  $('#'+id+' > h1')[0].scrollIntoView(false);
}

function scrollOnSamePage(id) {
  $('#'+id)[0].scrollIntoView();
}

function showContent(id) {
  $('.content').children().addClass('hidden');
  $('#'+id).removeClass('hidden');
}

window.onpopstate = function(data) {
  showContent(event.state);
  return true;
};