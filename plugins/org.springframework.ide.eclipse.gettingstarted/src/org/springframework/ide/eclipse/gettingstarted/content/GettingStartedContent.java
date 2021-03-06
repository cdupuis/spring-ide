/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.content;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.github.GithubClient;
import org.springframework.ide.eclipse.gettingstarted.github.Repo;
import org.springframework.ide.eclipse.gettingstarted.github.auth.AuthenticatedDownloader;
import org.springframework.ide.eclipse.gettingstarted.guides.GettingStartedGuide;
import org.springframework.ide.eclipse.gettingstarted.guides.ReferenceApp;
import org.springframework.ide.eclipse.gettingstarted.guides.ReferenceAppMetaData;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadManager;

/**
 * Singleton class. The instance of this class provides access to all the
 * getting started content.
 * 
 * NOTE: templates are not (yet?) included in this. Code to discover and
 * manage them already existed before this framework was implemented.
 */
public class GettingStartedContent extends ContentManager {

	private static GettingStartedContent INSTANCE = null;
	
	private final static boolean ADD_REAL =  (""+Platform.getLocation()).contains("kdvolder");
	private final static boolean ADD_MOCKS = (""+Platform.getLocation()).contains("kdvolder");
	
	public static GettingStartedContent getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new GettingStartedContent();
		}
		return INSTANCE;
	}
	
	private GithubClient github = new GithubClient(); 
	
	private GettingStartedContent() {
		//Guides: are discoverable because they are all repos in org on github
		register(GettingStartedGuide.class, new ContentProvider<GettingStartedGuide>() {
			@Override
			public GettingStartedGuide[] fetch(DownloadManager downloader) {
				List<GettingStartedGuide> guides = new ArrayList<GettingStartedGuide>();
				if (ADD_MOCKS) {
					addGuidesFrom(github.getUserRepos("kdvolder"), guides, downloader);
				}
				if (ADD_REAL) {
					addGuidesFrom(github.getOrgRepos("springframework-meta"), guides, downloader);
				}
				return guides.toArray(new GettingStartedGuide[guides.size()]);
			}
			
			private List<GettingStartedGuide> addGuidesFrom(Repo[] repos, List<GettingStartedGuide> guides, DownloadManager downloader) {
				for (Repo repo : repos) {
					String name = repo.getName();
					if (name.startsWith("gs-") && !name.equals("gs-redis-counters")) {
						//TODO: gs-redis-counters is special. Has no codesets only a readme.md
						//  can't deal with that right now. What should we do with this? 
						//  Are there any others like it?
						guides.add(new GettingStartedGuide(repo, downloader));
					}
				}
				return guides;
			}
		});
		
		//References apps: are discoverable because we maintain a list of json metadata
		//that can be downloaded from some external url.
		register(ReferenceApp.class, new ContentProvider<ReferenceApp>() {

			@Override
			public ReferenceApp[] fetch(DownloadManager downloader) {
				ReferenceAppMetaData[] infos = github.get("https://raw.github.com/kdvolder/spring-reference-apps-meta/master/reference-apps.json", ReferenceAppMetaData[].class);
				ReferenceApp[] apps = new ReferenceApp[infos.length];
				for (int i = 0; i < apps.length; i++) {
					//TODO: it could be quite costly to create all these since each one
					// entails a request to obtain info about github repo.
					// Maybe this is a good reason to put a bit more info in the
					// json metadata and thereby avoid querying github to fetch it.
					apps[i] = create(downloader, infos[i]);
				}
				return apps;
			}

			private ReferenceApp create(DownloadManager dl, ReferenceAppMetaData md) {
				return new ReferenceApp(md, dl, github);
			}
			
		});
	}
	
	/**
	 * Factory method to create a DownloadManager for a given content type name
	 */
	@Override
	public DownloadManager downloadManagerFor(Class<?> contentType) {
		return new DownloadManager(new AuthenticatedDownloader(), 
				new File(
						GettingStartedActivator.getDefault().getStateLocation().toFile(),
						contentType.getSimpleName()
				)
		);		
	}

	/**
	 * Get all getting started guides.
	 */
	public GettingStartedGuide[] getGuides() {
		return get(GettingStartedGuide.class);
	}

	public ReferenceApp[] getReferenceApps() {
		return get(ReferenceApp.class);
	}

	public GettingStartedGuide getGuide(String guideName) {
		GettingStartedGuide[] guides = getGuides();
		if (guides!=null) {
			for (GettingStartedGuide g : guides) {
				if (guideName.equals(g.getName())) {
					return g;
				}
			}
		}
		return null;
	}
	
}
