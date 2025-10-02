<template>
  <div class="admin-container">
    <LeftSidebar />
    <div class="main-content">
      <div class="admin-box">
        <div class="admin-header">
          <h2>管理面板</h2>
          <p>欢迎管理员回来！</p>
        </div>
        
        <el-tabs v-model="activeTab" class="admin-tabs" @tab-change="handleTabChange">
          <!-- 用户管理 -->
          <el-tab-pane label="用户管理" name="users">
            <div class="users-section">
              <div class="section-header">
                <h3>用户列表</h3>
                <el-input
                  v-model="userSearchKeyword"
                  placeholder="搜索用户名"
                  clearable
                  @input="handleUserSearch"
                  class="search-input"
                />
              </div>
              
              <el-table
                v-loading="loadingUsers"
                :data="usersData"
                style="width: 100%"
                :border="false"
                class="users-table"
              >
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="userName" label="用户名" width="180" />
                <el-table-column prop="role" label="角色" width="120">
                  <template #default="scope">
                    <el-tag :type="getRoleType(scope.row.role)">
                      {{ scope.row.role }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="200" />
                <el-table-column prop="lastLoginTime" label="最后登录时间" width="200" />
                <el-table-column label="操作" width="180" fixed="right">
                  <template #default="scope">
                    <el-button
                      type="primary"
                      size="small"
                      @click="editUserRole(scope.row)"
                      :disabled="scope.row.id === currentUserId || scope.row.role === 1"
                    >
                      修改角色
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      @click="deleteUser(scope.row.id)"
                      :disabled="scope.row.id === currentUserId || scope.row.role === 1"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <div class="pagination">
                <el-pagination
                  v-model:current-page="userCurrentPage"
                  v-model:page-size="userPageSize"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="userTotal"
                  @size-change="handleUserSizeChange"
                  @current-change="handleUserCurrentChange"
                />
              </div>
            </div>
          </el-tab-pane>
          
          <!-- 媒体管理 -->
          <el-tab-pane label="媒体管理" name="media">
            <div class="media-section">
              <div class="section-header">
                <h3>媒体列表</h3>
                <el-input
                  v-model="mediaSearchKeyword"
                  placeholder="搜索标题或描述"
                  clearable
                  @input="handleMediaSearch"
                  class="search-input"
                />
              </div>
              
              <el-table
                v-loading="loadingMedia"
                :data="mediaData"
                style="width: 100%"
                :border="false"
                class="media-table"
              >
                <el-table-column label="缩略图" width="120">
                  <template #default="scope">
                    <!-- 根据文件类型显示不同的内容 -->
                    <template v-if="isVideoFile(scope.row)">
                      <div class="video-thumbnail" @click="viewMediaDetail(scope.row)">
                        <video 
                          :src="getMediaUrl(scope.row)" 
                          class="thumbnail-video"
                          preload="metadata"
                          @click.stop
                        ></video>
                        <div class="video-overlay">
                          <span class="video-icon">▶</span>
                        </div>
                      </div>
                    </template>
                    <img 
                      v-else
                      :src="getMediaUrl(scope.row)" 
                      :alt="scope.row.title || '媒体文件'"
                      class="thumbnail-img" 
                      @error="handleImageError($event)"
                      @click="viewMediaDetail(scope.row)"
                    />
                  </template>
                </el-table-column>
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="fileTitle" label="标题" width="200" />
                <el-table-column prop="fileDescription" label="描述" show-overflow-tooltip>
                  <template #default="scope">
                    {{ scope.row.fileDescription || '无描述' }}
                  </template>
                </el-table-column>
                <el-table-column prop="uploaderName" label="上传者" width="120" />
                <el-table-column prop="createTime" label="上传时间" width="200" />
                <el-table-column label="操作" width="180" fixed="right">
                  <template #default="scope">
                    <el-button
                      type="primary"
                      size="small"
                      @click="editMedia(scope.row)"
                    >
                      修改
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      @click="deleteMedia(scope.row.id)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <div class="pagination">
                <el-pagination
                  v-model:current-page="mediaCurrentPage"
                  v-model:page-size="mediaPageSize"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="mediaTotal"
                  @size-change="handleMediaSizeChange"
                  @current-change="handleMediaCurrentChange"
                />
              </div>
            </div>
          </el-tab-pane>
          
          <!-- 标签管理 -->
          <el-tab-pane label="标签管理" name="tags">
            <div class="tags-section">
              <div class="section-header">
                <h3>标签列表</h3>
                <el-button type="primary" @click="showAddTagDialog = true">
                  <el-icon><Plus /></el-icon>
                  添加标签
                </el-button>
              </div>
              
              <el-table
                v-loading="loadingTags"
                :data="tagsData"
                style="width: 100%"
                :border="false"
                class="tags-table"
              >
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="tagName" label="标签名称" width="200" />
                <el-table-column prop="count" label="使用次数" width="120" />
                <el-table-column label="操作" width="120" fixed="right">
                  <template #default="scope">
                    <el-button
                      type="danger"
                      size="small"
                      @click="deleteTag(scope.row.id)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <div class="pagination">
                <el-pagination
                  v-model:current-page="tagCurrentPage"
                  v-model:page-size="tagPageSize"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="tagTotal"
                  @size-change="handleTagSizeChange"
                  @current-change="handleTagCurrentChange"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
  
  <!-- 修改用户角色对话框 -->
  <el-dialog
    v-model="showEditRoleDialog"
    title="修改用户角色"
    width="30%"
  >
    <div class="edit-role-content">
      <p>用户名: {{ selectedUser?.username }}</p>
      <el-form-item label="角色" :border="false">
        <el-select v-model="selectedRole" placeholder="请选择角色">
            <el-option label="1" value="1" />
            <el-option label="2" value="2" />
            <el-option label="3" value="3" />
          </el-select>
      </el-form-item>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showEditRoleDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmEditRole">确定</el-button>
      </span>
    </template>
  </el-dialog>
  
  <!-- 添加标签对话框 -->
  <el-dialog
    v-model="showAddTagDialog"
    title="添加标签"
    width="30%"
  >
    <div class="add-tag-content">
      <el-form :model="tagForm" :rules="tagRules" ref="tagFormRef" :border="false">
        <el-form-item prop="name">
          <el-input v-model="tagForm.name" placeholder="请输入标签名称" :maxlength="10" show-word-limit />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showAddTagDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddTag">确定</el-button>
      </span>
    </template>
  </el-dialog>
  
  <!-- 编辑媒体对话框 -->
  <el-dialog
    v-model="showEditMediaDialog"
    title="编辑媒体信息"
    width="50%"
  >
    <div class="edit-media-content">
      <el-form :model="editMediaForm" :rules="editMediaRules" :border="false">
        <el-form-item prop="fileTitle" label="标题">
          <el-input v-model="editMediaForm.fileTitle" placeholder="请输入标题" :maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item prop="fileDescription" label="描述">
          <el-input v-model="editMediaForm.fileDescription" placeholder="请输入描述" type="textarea" :rows="4" :maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showEditMediaDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmEditMedia">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import { ref, reactive, computed, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { get, post, delete as del } from '../utils/request.js';
import LeftSidebar from '../components/LeftSidebar.vue';
import { useUserStore } from '../store/userState.js';
import { Plus } from '@element-plus/icons-vue';
import { getMediaUrl, isVideoFile } from '../utils/mediaUtils.js';
import { useRouter } from 'vue-router';

export default {
  name: 'AdminPanel',
  components: {
    LeftSidebar,
    Plus
  },
  setup() {
    // 正确初始化Pinia store
    const userStore = useUserStore();
    const activeTab = ref('users');
    const router = useRouter();
    
    // 用户管理相关状态
    const usersData = ref([]);
    const loadingUsers = ref(false);
    const userCurrentPage = ref(1);
    const userPageSize = ref(10);
    const userTotal = ref(0);
    const userSearchKeyword = ref('');
    const currentUserId = ref(userStore.userInfo?.id);
    
    // 标签管理相关状态
    const tagsData = ref([]);
    const loadingTags = ref(false);
    const tagCurrentPage = ref(1);
    const tagPageSize = ref(10);
    const tagTotal = ref(0);
    
    // 对话框状态
    const showEditRoleDialog = ref(false);
    const selectedUser = ref(null);
    const selectedRole = ref('');
    
    const showAddTagDialog = ref(false);
    const tagForm = reactive({ name: '' });
    const tagRules = {
      name: [
        { required: true, message: '请输入标签名称', trigger: 'blur' },
        { min: 1, max: 10, message: '标签名称长度在 1 到 10 个字符', trigger: 'blur' }
      ]
    };
    
    // 标签表单引用（移到这里确保模板渲染时可用）
    const tagFormRef = ref(null);
    
    // 媒体管理相关状态
    const mediaData = ref([]);
    const loadingMedia = ref(false);
    const mediaCurrentPage = ref(1);
    const mediaPageSize = ref(10);
    const mediaTotal = ref(0);
    const mediaSearchKeyword = ref('');
    
    // 媒体编辑对话框状态
    const showEditMediaDialog = ref(false);
    const editMediaForm = reactive({ id: '', fileTitle: '', fileDescription: '' });
    const editMediaRules = {
      fileTitle: [
        { required: true, message: '请输入标题', trigger: 'blur' },
        { min: 1, max: 50, message: '标题长度在 1 到 50 个字符', trigger: 'blur' }
      ],
      fileDescription: [
        { max: 200, message: '描述长度不能超过200个字符', trigger: 'blur' }
      ]
    };
    
    // 加载用户列表
    const loadUsers = async () => {
      loadingUsers.value = true;
      try {
          const response = await get('/api/admin/users', {
            page: userCurrentPage.value,
            pageSize: userPageSize.value,
            keyword: userSearchKeyword.value
          });
        
        if (response.code === 1 || response.code === 200) {
            usersData.value = response.data.usersList || [];
            userTotal.value = response.data.total || 0;
          } else {
            ElMessage.error(response.message || '加载失败');
          }
      } catch (error) {
        ElMessage.error('加载失败，请稍后重试');
      } finally {
        loadingUsers.value = false;
      }
    };
    
    // 加载标签列表
    const loadTags = async () => {
      loadingTags.value = true;
      try {
          const response = await get('/api/admin/tags');
        
        if (response.code === 1 || response.code === 200) {
            tagsData.value = response.data || [];
            tagTotal.value = response.data?.length || 0;
          } else {
            ElMessage.error(response.message || '加载失败');
          }
      } catch (error) {
        ElMessage.error('加载失败，请稍后重试');
      } finally {
        loadingTags.value = false;
      }
    };
    
    // 处理用户搜索
    const handleUserSearch = () => {
      userCurrentPage.value = 1;
      loadUsers();
    };
    
    // 用户分页大小变化
    const handleUserSizeChange = (size) => {
      userPageSize.value = size;
      loadUsers();
    };
    
    // 用户当前页码变化
    const handleUserCurrentChange = (current) => {
      userCurrentPage.value = current;
      loadUsers();
    };
    
    // 标签分页大小变化
    const handleTagSizeChange = (size) => {
      tagPageSize.value = size;
      loadTags();
    };
    
    // 标签当前页码变化
    const handleTagCurrentChange = (current) => {
      tagCurrentPage.value = current;
      loadTags();
    };
    
    // 获取角色类型（用于标签样式）
    const getRoleType = (role) => {
      switch (role) {
        case 1:
          return 'danger';
        case 3:
          return 'success';
        default:
          return 'primary';
      }
    };
    
    // 获取角色名称（保留此函数，以备后续可能需要）
    const getRoleName = (role) => {
      switch (role) {
        case 1:
          return '管理员';
        case 3:
          return '亲密好友';
        default:
          return '普通用户';
      }
    };
    
    // 编辑用户角色
    const editUserRole = (user) => {
      selectedUser.value = { ...user };
      // 直接使用数据库中的角色值(1/2/3)
      selectedRole.value = user.role.toString();
      showEditRoleDialog.value = true;
    };
    
    // 确认编辑用户角色
    const confirmEditRole = async () => {
      if (!selectedUser.value || !selectedRole.value) {
        ElMessage.warning('请选择角色');
        return;
      }
      
      try {
          // 直接使用选择的角色值(转换为数字)
          const roleValue = parseInt(selectedRole.value);
          
          // 尝试将role参数直接附加到URL中
          const response = await post(`/api/admin/user/role/${selectedUser.value.id}?role=${roleValue}`, {});
        
        if (response.code === 1 || response.code === 200) {
            ElMessage.success('修改成功');
            showEditRoleDialog.value = false;
            loadUsers();
          } else {
            ElMessage.error(response.message || '修改失败');
          }
      } catch (error) {
        ElMessage.error('修改失败，请稍后重试');
      }
    };
    
    // 删除用户
    const deleteUser = async (userId) => {
      if (userId === currentUserId.value) {
        ElMessage.warning('不能删除当前登录用户');
        return;
      }
      
      try {
        await ElMessageBox.confirm('确定要删除这个用户吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        
        const response = await del(`/api/admin/delete-user?userId=${userId}`);
        
        if (response.code === 1 || response.code === 200) {
          ElMessage.success('删除成功');
          loadUsers();
        } else {
          ElMessage.error(response.message || '删除失败');
        }
      } catch (error) {
        if (error === 'cancel') {
          return;
        }
        ElMessage.error('删除失败，请稍后重试');
      }
    };
    
    // 确认添加标签
    const confirmAddTag = async () => {
      // 简化实现，直接使用表单数据进行验证
      if (!tagForm.name || tagForm.name.trim() === '') {
        ElMessage.error('请输入标签名称');
        return;
      }
      
      if (tagForm.name.length > 10) {
        ElMessage.error('标签名称长度不能超过10个字符');
        return;
      }
      
      // 手动验证通过后直接发送请求
      try {
            const response = await post('/api/admin/add-tag', {}, {
                params: {
                  tagName: tagForm.name
                }
              });
            
            if (response.code === 1 || response.code === 200) {
                ElMessage.success('添加成功');
                showAddTagDialog.value = false;
                tagForm.name = '';
                loadTags();
              } else {
                ElMessage.error(response.message || '添加失败');
              }
          } catch (error) {
            ElMessage.error('添加失败，请稍后重试');
          }
    };
    
    // 删除标签
    const deleteTag = async (tagId) => {
      try {
        await ElMessageBox.confirm('确定要删除这个标签吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        
        const response = await del(`/api/admin/delete-tag?tagId=${tagId}`);
        
        if (response.code === 1 || response.code === 200) {
          ElMessage.success('删除成功');
          loadTags();
        } else {
          ElMessage.error(response.message || '删除失败');
        }
      } catch (error) {
        if (error === 'cancel') {
          return;
        }
        ElMessage.error('删除失败，请稍后重试');
      }
    };
    
    // 加载媒体列表
    const loadMedia = async () => {
      loadingMedia.value = true;
      try {
          const response = await get('/api/media/public-media', {
            page: mediaCurrentPage.value,
            size: mediaPageSize.value,
            keyword: mediaSearchKeyword.value
          });
        
        if (response.code === 1 || response.code === 200) {
            // 处理媒体数据，映射字段名
            const rawData = response.data || [];
            // 映射字段名以匹配前端显示要求
            const mappedData = rawData.map(item => ({
              ...item,
              // 映射字段名
              fileTitle: item.title || item.fileTitle,
              fileDescription: item.description || item.fileDescription,
              uploaderName: item.uploaderName
            }));
            
            mediaData.value = mappedData;
            mediaTotal.value = response.total || 0;
          } else {
            ElMessage.error(response.message || '加载失败');
          }
      } catch (error) {
        ElMessage.error('加载失败，请稍后重试');
      } finally {
        loadingMedia.value = false;
      }
    };
    
    // 处理媒体搜索
    const handleMediaSearch = () => {
      mediaCurrentPage.value = 1;
      loadMedia();
    };
    
    // 媒体分页大小变化
    const handleMediaSizeChange = (size) => {
      mediaPageSize.value = size;
      loadMedia();
    };
    
    // 媒体当前页码变化
    const handleMediaCurrentChange = (current) => {
      mediaCurrentPage.value = current;
      loadMedia();
    };
    
    // 处理图片加载错误
    const handleImageError = (event) => {
      event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2ZmZiIvPjxwYXRoIGQ9Ik0zNiA3NGg2MnY2MmgtNjJ6IiBmaWxsPSIjZmZmIi8+PHBhdGggZD0iTTUyIDEzMmg0OHY0Nkg1MnYtNDZ6IiBmaWxsPSIjZmZmIi8+PHBhdGggZD0iTTY4IDkwdjE2aC0xNnYtMTZoMTZ6IiBmaWxsPSIjZmZmIi8+PHBhdGggZD0iTTgwIDE0MHYtMThoMzJ2MThoLTMyek02OCAxNDB2LTNoMzJ2M2gtMzJ6IiBmaWxsPSIjZmZmIi8+PHRleHQgeD0iMTAwIiB5PSI4MCIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZmlsbD0iIzMzMyIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjIwIj48dGV4dCB4PSIxMDAiIHk9IjgwIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjMzMzIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMjAiIGZpbGw9IiMzMzMiIHRleHQtYW5jaG9yPSJtaWRkbGUiPk5vIGltYWdlPC90ZXh0PjwvdGV4dD48L3N2Zz4=';
      event.target.alt = '无缩略图';
    };
    
    // 编辑媒体
    const editMedia = (media) => {
      editMediaForm.id = media.id;
      editMediaForm.fileTitle = media.fileTitle;
      editMediaForm.fileDescription = media.fileDescription || '';
      showEditMediaDialog.value = true;
    };
    
    // 确认编辑媒体
    const confirmEditMedia = async () => {
      try {
        const response = await post('/api/admin/update-media', {
          id: editMediaForm.id,
          fileTitle: editMediaForm.fileTitle,
          fileDescription: editMediaForm.fileDescription
        });
        
        if (response.code === 1 || response.code === 200) {
          ElMessage.success('修改成功');
          showEditMediaDialog.value = false;
          loadMedia();
        } else {
          ElMessage.error(response.message || '修改失败');
        }
      } catch (error) {
        ElMessage.error('修改失败，请稍后重试');
      }
    };
    
    // 删除媒体
    const deleteMedia = async (mediaId) => {
      try {
        await ElMessageBox.confirm('确定要删除这个媒体吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        
        const response = await del(`/api/admin/delete-media?id=${mediaId}`);
        
        if (response.code === 1 || response.code === 200) {
          ElMessage.success('删除成功');
          loadMedia();
        } else {
          ElMessage.error(response.message || '删除失败');
        }
      } catch (error) {
        if (error === 'cancel') {
          return;
        }
        ElMessage.error('删除失败，请稍后重试');
      }
    };
    
    // 查看媒体详情
    const viewMediaDetail = (media) => {
      router.push(`/media/${media.id}`);
    };
    
    // 组件挂载时加载数据
    onMounted(() => {
      loadUsers();
      loadTags();
      loadMedia();
    });

    // 切换标签页时加载对应数据
    const handleTabChange = (tabName) => {
      if (tabName === 'tags') {
        loadTags();
      } else if (tabName === 'users') {
        loadUsers();
      } else if (tabName === 'media') {
        loadMedia();
      }
    };
    
    return {
      userStore,
      activeTab,
      usersData,
      loadingUsers,
      userCurrentPage,
      userPageSize,
      userTotal,
      userSearchKeyword,
      currentUserId,
      tagsData,
      loadingTags,
      tagCurrentPage,
      tagPageSize,
      tagTotal,
      showEditRoleDialog,
      selectedUser,
      selectedRole,
      showAddTagDialog,
      tagForm,
      tagRules,
      handleUserSearch,
      handleUserSizeChange,
      handleUserCurrentChange,
      handleTagSizeChange,
      handleTagCurrentChange,
      getRoleType,
      getRoleName,
      editUserRole,
      confirmEditRole,
      deleteUser,
      confirmAddTag,
      deleteTag,
      handleTabChange,
      
      // 媒体管理相关
      mediaData,
      loadingMedia,
      mediaCurrentPage,
      mediaPageSize,
      mediaTotal,
      mediaSearchKeyword,
      showEditMediaDialog,
      editMediaForm,
      editMediaRules,
      handleMediaSearch,
      handleMediaSizeChange,
      handleMediaCurrentChange,
      getMediaUrl,
      isVideoFile,
      handleImageError,
      editMedia,
      confirmEditMedia,
      deleteMedia,
      viewMediaDetail
    };
  }
};
</script>

<style scoped>
.admin-container {
  display: flex;
  min-height: 100vh;
  background-color: #f9f9f9;
}

.main-content {
  flex: 1;
  padding: 20px;
  margin-left: 200px; /* 避开固定的左侧导航栏 */
}

.admin-box {
  background-color: #fff;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.admin-box::before {
  content: '';
  position: absolute;
  top: -100px;
  right: -100px;
  width: 300px;
  height: 300px;
  background-color: rgba(204, 204, 255, 0.1);
  border-radius: 50%;
  z-index: 0;
}

.admin-box::after {
  content: '';
  position: absolute;
  bottom: -100px;
  left: -100px;
  width: 250px;
  height: 250px;
  background-color: rgba(204, 255, 204, 0.1);
  border-radius: 50%;
  z-index: 0;
}

.admin-header,
.admin-tabs {
  position: relative;
  z-index: 1;
}

.admin-header {
  text-align: center;
  margin-bottom: 40px;
}

.admin-header h2 {
  margin: 0 0 10px 0;
  font-size: 32px;
  color: #6b6bff;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.admin-header p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.admin-tabs {
  margin-bottom: 20px;
}

.admin-tabs .el-tabs__header {
  margin-bottom: 30px;
}

.admin-tabs .el-tabs__nav {
  justify-content: center;
}

.admin-tabs .el-tabs__item {
  font-size: 18px;
  padding: 0 30px;
}

.users-section,
  .tags-section,
  .media-section {
    position: relative;
    z-index: 1;
  }

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  font-size: 20px;
  color: #333;
  font-weight: bold;
}

.search-input {
  width: 300px;
}

.users-table,
  .tags-table,
  .media-table {
    margin-bottom: 20px;
    border-radius: 10px;
    overflow: hidden;
  }

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.edit-role-content {
  padding: 20px 0;
}

.edit-role-content p {
  margin-bottom: 20px;
  color: #666;
}

.add-tag-content {
  padding: 20px 0;
}

.edit-media-content {
    padding: 20px 0;
  }
  
  .thumbnail-img {
  width: 100%;
  height: 80px;
  object-fit: cover;
  border-radius: 5px;
}

/* 视频缩略图样式 */
.video-thumbnail {
  position: relative;
  width: 100%;
  height: 80px;
  border-radius: 5px;
  overflow: hidden;
}

.thumbnail-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.video-thumbnail:hover .video-overlay {
  opacity: 1;
}

.video-icon {
  color: white;
  font-size: 24px;
  font-weight: bold;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 儿童风格的设计元素 */
.admin-box {
  border: none;
}

.admin-box::before {
  background-color: rgba(107, 107, 255, 0.1);
  animation: float 8s ease-in-out infinite;
}

.admin-box::after {
  background-color: rgba(107, 255, 107, 0.1);
  animation: float 10s ease-in-out infinite 1s;
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-30px);
  }
  100% {
    transform: translateY(0px);
  }
}



/* 响应式设计 */
@media (max-width: 768px) {
  .admin-box {
    margin: 0 20px;
    padding: 30px 20px;
  }
  
  .admin-header h2 {
    font-size: 24px;
  }
  
  .section-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .search-input {
    width: 100%;
  }
  
  .pagination {
    justify-content: center;
  }
}
</style>