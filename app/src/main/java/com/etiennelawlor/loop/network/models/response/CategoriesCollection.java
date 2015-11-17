package com.etiennelawlor.loop.network.models.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

//{
//        "total": 16,
//        "page": 1,
//        "per_page": 2,
//        "paging": {
//        "next": "/categories?per_page=2&page=2",
//        "previous": null,
//        "first": "/categories?per_page=2&page=1",
//        "last": "/categories?per_page=2&page=8"
//        },
//        "data": [
//        {
//        "uri": "/categories/animation",
//        "name": "Animation",
//        "link": "https://vimeo.com/categories/animation",
//        "top_level": true,
//        "pictures": {
//        "uri": "/videos/658158/pictures/25153220",
//        "active": true,
//        "sizes": [
//        {
//        "width": 100,
//        "height": 75,
//        "link": "https://i.vimeocdn.com/video/25153220_100x75.jpg"
//        },
//        {
//        "width": 200,
//        "height": 150,
//        "link": "https://i.vimeocdn.com/video/25153220_200x150.jpg"
//        },
//        {
//        "width": 295,
//        "height": 166,
//        "link": "https://i.vimeocdn.com/video/25153220_295x166.jpg"
//        },
//        {
//        "width": 640,
//        "height": 360,
//        "link": "https://i.vimeocdn.com/video/25153220_640x360.jpg"
//        },
//        {
//        "width": 960,
//        "height": 540,
//        "link": "https://i.vimeocdn.com/video/25153220_960x540.jpg"
//        }
//        ]
//        },
//        "parent": [],
//        "metadata": {
//        "connections": {
//        "channels": {
//        "uri": "/categories/animation/channels",
//        "options": [
//        "GET"
//        ],
//        "total": 31608
//        },
//        "groups": {
//        "uri": "/categories/animation/groups",
//        "options": [
//        "GET"
//        ],
//        "total": 8169
//        },
//        "users": {
//        "uri": "/categories/animation/users",
//        "options": [
//        "GET"
//        ],
//        "total": 1819
//        },
//        "videos": {
//        "uri": "/categories/animation/videos",
//        "options": [
//        "GET"
//        ],
//        "total": 283439
//        }
//        }
//        },
//        "subcategories": [
//        {
//        "uri": "/categories/2d",
//        "name": "2D",
//        "link": "https://vimeo.com/categories/animation/2d"
//        },
//        {
//        "uri": "/categories/3d",
//        "name": "3D/CG",
//        "link": "https://vimeo.com/categories/animation/3d"
//        },
//        {
//        "uri": "/categories/mograph",
//        "name": "Mograph",
//        "link": "https://vimeo.com/categories/animation/mograph"
//        },
//        {
//        "uri": "/categories/projectionmapping",
//        "name": "Projection Mapping",
//        "link": "https://vimeo.com/categories/animation/projectionmapping"
//        },
//        {
//        "uri": "/categories/stopmotion",
//        "name": "Stop Frame",
//        "link": "https://vimeo.com/categories/animation/stopmotion"
//        },
//        {
//        "uri": "/categories/vfx",
//        "name": "VFX",
//        "link": "https://vimeo.com/categories/animation/vfx"
//        }
//        ]
//        },
//        {
//        "uri": "/categories/art",
//        "name": "Arts & Design",
//        "link": "https://vimeo.com/categories/art",
//        "top_level": true,
//        "pictures": {
//        "uri": "/videos/1989281/pictures/86404950",
//        "active": true,
//        "sizes": [
//        {
//        "width": 100,
//        "height": 75,
//        "link": "https://i.vimeocdn.com/video/86404950_100x75.jpg"
//        },
//        {
//        "width": 200,
//        "height": 150,
//        "link": "https://i.vimeocdn.com/video/86404950_200x150.jpg"
//        },
//        {
//        "width": 295,
//        "height": 166,
//        "link": "https://i.vimeocdn.com/video/86404950_295x166.jpg"
//        },
//        {
//        "width": 640,
//        "height": 360,
//        "link": "https://i.vimeocdn.com/video/86404950_640x360.jpg"
//        },
//        {
//        "width": 960,
//        "height": 540,
//        "link": "https://i.vimeocdn.com/video/86404950_960x540.jpg"
//        }
//        ]
//        },
//        "parent": [],
//        "metadata": {
//        "connections": {
//        "channels": {
//        "uri": "/categories/art/channels",
//        "options": [
//        "GET"
//        ],
//        "total": 62213
//        },
//        "groups": {
//        "uri": "/categories/art/groups",
//        "options": [
//        "GET"
//        ],
//        "total": 15913
//        },
//        "users": {
//        "uri": "/categories/art/users",
//        "options": [
//        "GET"
//        ],
//        "total": 7842
//        },
//        "videos": {
//        "uri": "/categories/art/videos",
//        "options": [
//        "GET"
//        ],
//        "total": 172510
//        }
//        }
//        },
//        "subcategories": [
//        {
//        "uri": "/categories/architecture",
//        "name": "Architecture",
//        "link": "https://vimeo.com/categories/art/architecture"
//        },
//        {
//        "uri": "/categories/cars",
//        "name": "Cars",
//        "link": "https://vimeo.com/categories/art/cars"
//        },
//        {
//        "uri": "/categories/homesandliving",
//        "name": "Homes & Living",
//        "link": "https://vimeo.com/categories/art/homesandliving"
//        },
//        {
//        "uri": "/categories/installation",
//        "name": "Installation",
//        "link": "https://vimeo.com/categories/art/installation"
//        },
//        {
//        "uri": "/categories/performance",
//        "name": "Performance",
//        "link": "https://vimeo.com/categories/art/performance"
//        },
//        {
//        "uri": "/categories/personaltechdesign",
//        "name": "Personal Tech",
//        "link": "https://vimeo.com/categories/art/personaltechdesign"
//        },
//        {
//        "uri": "/categories/artworld",
//        "name": "The Art World",
//        "link": "https://vimeo.com/categories/art/artworld"
//        }
//        ]
//        }
//        ]
//        }

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class CategoriesCollection {

    // region Fields
    @SerializedName("total")
    private Integer total;
    @SerializedName("page")
    private Integer page;
    @SerializedName("per_page")
    private Integer perPage;
    @SerializedName("paging")
    private Paging paging;
    @SerializedName("data")
    private List<Category> categories;
    // endregion

    // region Getters
    public Integer getTotal() {
        return total == null ? -1 : total;
    }

    public Integer getPage() {
        return page == null ? -1 : page;
    }

    public Integer getPerPage() {
        return perPage == null ? -1 : perPage;
    }

    public Paging getPaging() {
        return paging;
    }

    public List<Category> getCategories() {
        return categories;
    }
    // endregion

    // region Setters
    public void setTotal(Integer total) {
        this.total = total;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setPerPage(Integer perPage) {
        this.perPage = perPage;
    }

    public void setPaging(Paging paging) {
        this.paging = paging;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
    // endregion
}
