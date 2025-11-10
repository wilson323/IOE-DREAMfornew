// 临时脚本：批量优化搜索栏的CSS和JS代码模板

// CSS模板
const cssTemplate = `        /* 可展开搜索按钮 */
        .search-toggle-btn {
            position: relative;
            display: inline-flex;
            align-items: center;
            gap: var(--space-xs);
            padding: var(--space-xs) var(--space-md);
            background: white;
            border: 1px solid var(--gray-300);
            border-radius: var(--radius-md);
            color: var(--gray-700);
            cursor: pointer;
            transition: all var(--transition);
            font-size: var(--text-sm);
        }
        .search-toggle-btn:hover {
            border-color: var(--primary-500);
            background: var(--primary-50);
            color: var(--primary-700);
        }
        .search-toggle-btn.active {
            border-color: var(--primary-500);
            background: var(--primary-600);
            color: white;
        }
        .search-dropdown {
            position: absolute;
            top: calc(100% + 8px);
            right: 0;
            width: 500px;
            max-width: 90vw;
            background: white;
            border-radius: var(--radius-lg);
            box-shadow: var(--shadow-xl);
            border: 1px solid var(--gray-200);
            padding: var(--space-lg);
            z-index: 100;
            display: none;
            animation: slideDown 0.2s ease;
        }
        @keyframes slideDown {
            from {
                opacity: 0;
                transform: translateY(-10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        .search-dropdown.show {
            display: block;
        }
        .filter-section {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: var(--space-md);
            margin-bottom: var(--space-md);
        }
        .filter-group {
            display: flex;
            flex-direction: column;
            gap: var(--space-xs);
        }
        .filter-group label {
            font-weight: 500;
            color: var(--gray-700);
            font-size: var(--text-xs);
            white-space: nowrap;
        }
        .filter-group input,
        .filter-group select {
            padding: var(--space-xs) var(--space-sm);
            border: 1px solid var(--gray-300);
            border-radius: var(--radius-md);
            background: white;
            font-size: var(--text-sm);
        }
        .filter-actions {
            display: flex;
            gap: var(--space-xs);
            justify-content: flex-end;
            padding-top: var(--space-md);
            border-top: 1px solid var(--gray-200);
            grid-column: 1 / -1;
        }
        .filter-actions .btn {
            padding: var(--space-xs) var(--space-md);
            font-size: var(--text-sm);
        }`;

// JavaScript模板
const jsTemplate = `
        // 切换搜索面板
        function toggleSearch() {
            const dropdown = document.getElementById('searchDropdown');
            const toggle = document.getElementById('searchToggle');
            const chevron = toggle.querySelector('.fa-chevron-down');
            
            dropdown.classList.toggle('show');
            toggle.classList.toggle('active');
            
            if (dropdown.classList.contains('show')) {
                chevron.style.transform = 'rotate(180deg)';
            } else {
                chevron.style.transform = 'rotate(0deg)';
            }
        }

        // 执行搜索
        function performSearch() {
            console.log('执行搜索');
            alert('搜索功能开发中...');
            toggleSearch();
        }

        // 重置搜索
        function resetSearch() {
            const inputs = document.querySelectorAll('#searchDropdown input');
            const selects = document.querySelectorAll('#searchDropdown select');
            
            inputs.forEach(input => input.value = '');
            selects.forEach(select => select.selectedIndex = 0);
            
            console.log('搜索条件已重置');
        }

        // 导出Excel
        function exportExcel() {
            alert('导出Excel功能开发中...');
        }

        // 点击外部关闭搜索面板
        document.addEventListener('click', function(event) {
            const dropdown = document.getElementById('searchDropdown');
            const toggle = document.getElementById('searchToggle');
            
            if (dropdown && toggle) {
                if (!dropdown.contains(event.target) && !toggle.contains(event.target)) {
                    if (dropdown.classList.contains('show')) {
                        toggleSearch();
                    }
                }
            }
        });`;

console.log('模板已生成，用于批量优化搜索栏');

